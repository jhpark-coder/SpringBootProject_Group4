$(document).ready(function () {
    function openPopup(message) {
        $("#popupMessage").text(message);
        $("#myPopup").addClass('show');
    }
    $("#closePopup").on("click", function () {
        $("#myPopup").removeClass('show');
    });

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

    // 기존 값 설정 (서버에서 전달된 데이터)
    var birthYear = $(".birthdate-group").data("year");
    var birthMonth = $(".birthdate-group").data("month");
    var birthDay = $(".birthdate-group").data("day");

    if (birthYear) {
        $("#birthYear").val(birthYear);
    }
    if (birthMonth) {
        $("#birthMonth").val(birthMonth);
    }
    if (birthDay) {
        $("#birthDay").val(birthDay);
    }

    // 폼 제출 처리
    $("#registerForm").on("submit", function (e) {
        e.preventDefault();

        var formData = {
            name: $("#name").val(),
            email: $("#email").val(),
            gender: $("input[name='gender']:checked").val(),
            birthYear: $("#birthYear").val(),
            birthMonth: $("#birthMonth").val(),
            birthDay: $("#birthDay").val()
        };

        $.ajax({
            url: $(this).attr('action'),
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(formData),
            success: function (response) {
                openPopup("개인정보가 성공적으로 수정되었습니다.");
                if (window.currentUser && window.currentUser.id) {
                    window.location.href = "/member/myPage/" + window.currentUser.id;
                } else {
                    window.location.href = "/"; // ID 정보가 없으면 메인으로
                }
            },
            error: function (xhr) {
                if (xhr.status === 400) {
                    var errors = xhr.responseJSON;
                    if (errors.globalError) {
                        openPopup(errors.globalError);
                    } else {
                        openPopup("입력 정보를 확인해주세요.");
                    }
                } else {
                    openPopup("알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
                }
            }
        });
    });

    // CSRF 토큰 설정
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $(document).ajaxSend(function (e, xhr, options) {
        if (header && token) {
            xhr.setRequestHeader(header, token);
        }
    });
}); 