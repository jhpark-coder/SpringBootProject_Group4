$(document).ready(function(){
      function openPopup(message){
        $("#popupMessage").text(message);
        $("#myPopup").addClass('show');
      }
      $("#closePopup").on("click", function() {
          $("#myPopup").removeClass('show');
      });

      var errorMessage = /*[[${errorMessage}]]*/ null;
      if(errorMessage != null){
        openPopup(errorMessage);
      }
      // 년도 생성 (1950년 ~ 현재년도)
      var currentYear = new Date().getFullYear();
      for (var year = currentYear; year >= 1950; year--) {
        $("#birthYear").append('<option value="' + year + '">' + year + '년</option>');
      }

      // 월 생성 (1~12월)
      for (var month = 1; month <= 12; month++) {
        $("#birthMonth").append('<option value="' + month + '">' + month + '월</option>');
      }

        // 일 생성 (1~31일)
      for (var day = 1; day <= 31; day++) {
        $("#birthDay").append('<option value="' + day + '">' + day + '일</option>');
      }


          // 3. 최종 회원가입 함수 (form의 submit 버튼 클릭 시 호출)
          $("#registerForm").on("submit", function(e) {
            // 1. 기본 폼 제출(새로고침)을 막습니다.
            e.preventDefault();
            if (!isEmailVerified) {
                openPopup("이메일 인증을 먼저 완료해주세요.");
                return;
            }
            var formData = {
                name: $("#name").val(),
                email: $("#email").val(),
                password: $("#password").val(),
                passwordConfirm: $("#passwordConfirm").val(),
                gender: $("input[name='gender']:checked").val(),
                birthYear: $("#birthYear").val(),
                birthMonth: $("#birthMonth").val(),
                birthDay: $("#birthDay").val()
            };
            $.ajax({
                url: $(this).attr('action'), // form의 action 속성값('/members/api/new')을 사용
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(formData),

                // [변경 4-1] 요청 성공 시 (HTTP 상태 코드 2xx)
                success: function(response) {
                    // 성공 팝업을 띄우고
                    openPopup(response);
                    // 팝업의 '확인' 버튼을 누르면 로그인 페이지로 이동하도록 이벤트 재정의
                    $("#closePopup").off('click').on('click', function() {
                        window.location.href = "/members/login";
                    });
                },
                 // [변경 4-2] 요청 실패 시 (HTTP 상태 코드 4xx, 5xx)
                error: function(xhr) {
                    // 이전에 표시된 모든 에러 메시지를 지웁니다.
                    $(".field-error").text("");

                    if (xhr.status === 400) {
                        var errors = xhr.responseJSON; // 컨트롤러가 보낸 에러 Map 객체

                        // 전역 에러(예: 이메일 중복)가 있으면 팝업으로 표시
                        if (errors.globalError) {
                            openPopup(errors.globalError);
                        }

                        // 각 필드별 에러 메시지를 해당 위치에 표시
                        $.each(errors, function(fieldName, errorMessage) {
                            $("#error-" + fieldName).text(errorMessage);
                        });
                    } else {
                        // 400 외 다른 에러(서버 내부 오류 등)는 팝업으로 표시
                        openPopup("알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
                    }
                }
            });
         });
        // [핵심] 이메일 인증 상태를 저장할 전역 변수
        var isEmailVerified = false;

        // CSRF 토큰 설정 (Spring Security 사용 시)
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        // 토큰이 없으면 경고 출력
        if (!token || !header) {
            console.error("CSRF 토큰을 찾을 수 없습니다.");
            console.log("Available meta tags:", $("meta").map(function() { return $(this).attr('name'); }).get());
        }

        $(document).ajaxSend(function(e, xhr, options) {
            if (header && token) {
                xhr.setRequestHeader(header, token);
                console.log("CSRF 헤더 설정됨:", header, token);
            } else {
                console.warn("CSRF 헤더 설정 실패");
            }
        });

        // 1. 인증메일 발송 함수
          $("#sendAuthEmailBtn").on("click", function() {
            console.log("sendAuthEmail 함수 호출됨");

            var email = $("#email").val();
            console.log("입력된 이메일:", email);

            if (!email) {

                openPopup("이메일을 입력해주세요!");
                return;
            }

            console.log("AJAX 요청 시작...");
            $.ajax({
                url: "/members/email-auth",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify({ email: email }),
                success: function(response) {

                    openPopup(response);
                    $("#verifyEmailBtn").prop("disabled", false);
                },
                error: function(xhr, status, error) {
                    openPopup("오류 발생: " + xhr.responseText);
                }
            });
          });

        // 2. 인증코드 확인 함수
        $("#verifyEmailBtn").on("click", function(){
            var email = $("#email").val();
            var authCode = $("#authCode").val();

            $.ajax({
                url: "/members/email-verify",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify({ email: email, authCode: authCode }),
                success: function(response) {
                    openPopup(response);
                    $("#emailVerifyMessage").text("인증 완료");
                    isEmailVerified = true;

                    $("#email").prop("readonly", true);
                    $("#email").addClass("readonly-as-disabled");

                    $("#sendAuthEmailBtn").prop("disabled", true);
                    $("#authCode").prop("disabled", true);
                    $("#verifyEmailBtn").prop("disabled", true);


                    checkFormValidity();
                },
                error: function(xhr) {
                    openPopup(xhr.responseText);
                    isEmailVerified = false;
                    checkFormValidity();
                }
            });
        });



        // [핵심] 회원가입 버튼 활성화 여부를 체크하는 함수
        function checkFormValidity() {
            if (isEmailVerified) {
                $("#submitBtn").prop("disabled", false);
            } else {
                $("#submitBtn").prop("disabled", true);
            }
        }
});