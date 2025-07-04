$(document).ready(function() {

    // =========================================================================
    // 1. [수정] HTML의 data-* 속성에서 사용자 생년월일 정보를 가져옵니다.
    // =========================================================================
    const birthdateGroup = $('.birthdate-group');
    const userBirthYear = birthdateGroup.data('year');
    const userBirthMonth = birthdateGroup.data('month');
    const userBirthDay = birthdateGroup.data('day');

    // =================================================
    // 2. [유지] 년/월/일 <option> 동적 생성 로직 (코드는 동일)
    // =================================================
    var currentYear = new Date().getFullYear();
    for (var year = currentYear; year >= 1950; year--) {
        $("#birthYear").append('<option value="' + year + '">' + year + '년</option>');
    }
    // ... (월, 일 생성 로직은 동일) ...
    for (var month = 1; month <= 12; month++) {
        $("#birthMonth").append('<option value="' + month + '">' + month + '월</option>');
    }
    for (var day = 1; day <= 31; day++) {
        $("#birthDay").append('<option value="' + day + '">' + day + '일</option>');
    }

    // ===================================================================================
    // 3. [유지] 생성된 <option> 중에서, 위에서 받은 정보로 기본값을 선택합니다 (코드는 동일)
    // ===================================================================================
    if (userBirthYear) {
        $('#birthYear').val(userBirthYear);
    }
    if (userBirthMonth) {
        $('#birthMonth').val(userBirthMonth);
    }
    if (userBirthDay) {
        $('#birthDay').val(userBirthDay);
    }
});