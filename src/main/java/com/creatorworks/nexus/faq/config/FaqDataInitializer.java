package com.creatorworks.nexus.faq.config;

import com.creatorworks.nexus.faq.entity.Faq;
import com.creatorworks.nexus.faq.entity.FaqCategory;
import com.creatorworks.nexus.faq.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FaqDataInitializer implements CommandLineRunner {
    
    private final FaqRepository faqRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (faqRepository.count() == 0) {
            log.info("FAQ 초기 데이터를 생성합니다.");
            initializeFaqData();
        }
    }
    
    private void initializeFaqData() {
        List<Faq> faqs = Arrays.asList(
            // 회원가입/로그인
            createFaq(FaqCategory.MEMBERSHIP, 1, 
                "회원가입은 어떻게 하나요?", 
                "일반 회원가입과 SNS(네이버, 카카오, 구글 등) 간편가입을 통해 가입하실 수 있습니다. 홈페이지 상단의 '회원가입' 버튼을 클릭하시면 가입 방법을 확인하실 수 있습니다."),
            
            createFaq(FaqCategory.MEMBERSHIP, 2, 
                "아이디/비밀번호를 잊어버렸어요.", 
                "로그인 페이지 하단의 '아이디/비밀번호 찾기'를 통해 본인인증 후 찾으실 수 있습니다. 이메일 인증을 통해 새로운 비밀번호를 설정하실 수 있습니다."),
            
            createFaq(FaqCategory.MEMBERSHIP, 3, 
                "SNS 계정으로 가입했는데, 비밀번호를 변경하고 싶어요.", 
                "SNS 간편가입 회원은 해당 SNS 사이트에서 직접 비밀번호를 변경하셔야 합니다. Nexus에서는 SNS 계정의 비밀번호를 변경할 수 없습니다."),
            
            createFaq(FaqCategory.MEMBERSHIP, 4, 
                "로그인이 계속 실패해요.", 
                "아이디와 비밀번호를 다시 한번 확인해주시고, 문제가 지속될 경우 고객센터로 문의해주세요. 대소문자 구분과 특수문자 입력을 확인해주세요."),
            
            // 작품 구매 및 이용
            createFaq(FaqCategory.PURCHASE, 1, 
                "작품은 어떻게 구매하나요?", 
                "원하는 작품 상세 페이지에서 '구매하기' 버튼을 누른 후, 결제를 진행하시면 즉시 다운로드할 수 있습니다. 결제 완료 후 마이페이지에서 다운로드할 수 있습니다."),
            
            createFaq(FaqCategory.PURCHASE, 2, 
                "미리보기와 원본의 차이가 무엇인가요?", 
                "미리보기는 저화질 또는 워터마크가 포함된 버전으로, 내용을 확인하는 용도입니다. 결제 후에는 워터마크가 없는 고화질 원본 파일을 다운로드할 수 있습니다."),
            
            createFaq(FaqCategory.PURCHASE, 3, 
                "구매한 작품은 어디서 다시 볼 수 있나요?", 
                "로그인 후 '마이페이지 > 구매 내역'에서 언제든지 다시 확인하고 다운로드할 수 있습니다. 구매한 작품은 영구적으로 보관됩니다."),
            
            createFaq(FaqCategory.PURCHASE, 4, 
                "작품의 저작권 및 사용 범위는 어떻게 되나요?", 
                "구매한 작품은 개인적인 용도 또는 명시된 상업적 허용 범위 내에서 사용할 수 있습니다. 재판매, 재배포는 엄격히 금지됩니다. 자세한 내용은 각 작품별 라이선스 규정을 확인해주세요."),
            
            createFaq(FaqCategory.PURCHASE, 5, 
                "다운로드 기간에 제한이 있나요?", 
                "아니요, 한번 구매한 작품은 영구적으로 다운로드 가능합니다. 언제든지 마이페이지에서 다시 다운로드하실 수 있습니다."),
            
            // 결제/환불
            createFaq(FaqCategory.PAYMENT, 1, 
                "어떤 결제 수단을 이용할 수 있나요?", 
                "신용카드, 실시간 계좌이체, 가상계좌, 휴대폰 소액결제 등 이니시스에서 제공하는 다양한 결제 수단을 이용하실 수 있습니다."),
            
            createFaq(FaqCategory.PAYMENT, 2, 
                "결제 중 오류가 발생했어요.", 
                "일시적인 네트워크 문제일 수 있습니다. 잠시 후 다시 시도해보시고, 동일한 문제가 발생하면 오류 메시지를 캡처하여 고객센터로 문의해주세요."),
            
            createFaq(FaqCategory.PAYMENT, 3, 
                "현금영수증이나 세금계산서 발행이 가능한가요?", 
                "네, 결제 과정에서 신청하시거나 '마이페이지 > 결제 내역'에서 직접 발행하실 수 있습니다."),
            
            createFaq(FaqCategory.PAYMENT, 4, 
                "환불 규정은 어떻게 되나요?", 
                "디지털 콘텐츠 특성상, 다운로드를 한 번이라도 진행한 경우에는 환불이 불가능합니다. 다운로드 이력이 없는 경우, 결제 후 7일 이내에 고객센터를 통해 환불 신청이 가능합니다."),
            
            createFaq(FaqCategory.PAYMENT, 5, 
                "환불은 언제 처리되나요?", 
                "환불 요청이 승인되면 결제 수단에 따라 영업일 기준 3~5일 이내에 처리됩니다."),
            
            // 구독 서비스
            createFaq(FaqCategory.SUBSCRIPTION, 1, 
                "구독 서비스는 어떤 혜택이 있나요?", 
                "구독 회원은 매월 일정 개수의 작품을 무료로 다운로드하거나, 모든 작품을 할인된 가격에 구매할 수 있는 혜택을 드립니다. 구체적인 혜택은 구독 플랜에 따라 다릅니다."),
            
            createFaq(FaqCategory.SUBSCRIPTION, 2, 
                "구독료는 어떻게 결제되나요?", 
                "구독 신청 시 등록한 결제 수단으로 매월 자동 결제됩니다. 결제일은 구독 시작일을 기준으로 매월 같은 날에 진행됩니다."),
            
            createFaq(FaqCategory.SUBSCRIPTION, 3, 
                "구독을 해지하고 싶어요.", 
                "'마이페이지 > 구독 관리'에서 언제든지 해지할 수 있으며, 다음 결제일부터 요금이 청구되지 않습니다."),
            
            createFaq(FaqCategory.SUBSCRIPTION, 4, 
                "구독을 해지하면 이전에 받은 자료는 어떻게 되나요?", 
                "구독 기간 중에 다운로드한 자료는 해지 후에도 계속 이용하실 수 있습니다."),
            
            // 경매
            createFaq(FaqCategory.AUCTION, 1, 
                "경매에는 어떻게 참여하나요?", 
                "경매가 진행 중인 작품 페이지에서 '입찰하기' 버튼을 눌러 원하는 가격을 입력하면 참여할 수 있습니다."),
            
            createFaq(FaqCategory.AUCTION, 2, 
                "최고가로 낙찰받으면 어떻게 해야 하나요?", 
                "낙찰 안내를 받은 후, 24시간 이내에 결제를 완료해야 합니다. 시간 내에 결제하지 않으면 낙찰이 취소될 수 있습니다."),
            
            createFaq(FaqCategory.AUCTION, 3, 
                "경매 입찰을 취소할 수 있나요?", 
                "아니요, 한번 입찰한 내역은 신중한 결정을 위해 취소하거나 변경할 수 없습니다."),
            
            // 계정 및 정보 관리
            createFaq(FaqCategory.ACCOUNT, 1, 
                "회원정보(닉네임, 연락처 등)를 수정하고 싶어요.", 
                "로그인 후 '마이페이지 > 회원정보 수정' 메뉴에서 직접 변경하실 수 있습니다."),
            
            createFaq(FaqCategory.ACCOUNT, 2, 
                "비밀번호를 변경하고 싶어요.", 
                "'마이페이지 > 비밀번호 변경'에서 현재 비밀번호를 입력하신 후, 새 비밀번호로 변경할 수 있습니다. 만약 현재 비밀번호를 잊으셨다면, 로그아웃 후 '비밀번호 찾기'를 이용해주세요."),
            
            createFaq(FaqCategory.ACCOUNT, 3, 
                "광고성 이메일이나 SMS를 받고 싶지 않아요.", 
                "'마이페이지 > 회원정보 수정' 페이지 하단에서 마케팅 정보 수신 동의 여부를 변경하실 수 있습니다. 단, 서비스 이용과 관련된 중요 공지(결제, 약관 변경 등)는 수신 동의와 관계없이 발송됩니다."),
            
            createFaq(FaqCategory.ACCOUNT, 4, 
                "SNS 계정 연동을 추가하거나 해제하고 싶어요.", 
                "'마이페이지 > 소셜 로그인 연동 관리'에서 다른 SNS 계정을 추가로 연동하거나 기존 연동을 해제할 수 있습니다. 단, 모든 연동을 해제하기 전에, 사이트 로그인을 위한 이메일과 비밀번호를 반드시 설정해주세요."),
            
            createFaq(FaqCategory.ACCOUNT, 5, 
                "회원탈퇴는 어떻게 하나요?", 
                "회원탈퇴는 '마이페이지 > 회원탈퇴' 메뉴에서 신청할 수 있습니다. 탈퇴 시 계정의 모든 정보(구매 내역, 다운로드 권한, 구독 정보 등)는 즉시 삭제되며 복구가 불가능하니 신중하게 결정해주세요."),
            
            // 기타
            createFaq(FaqCategory.ETC, 1, 
                "상담원과 직접 상담하고 싶어요.", 
                "저희 고객센터는 평일 오전 10시부터 오후 6시까지 운영됩니다. (점심시간: 12:00~13:00, 주말/공휴일 휴무) 1:1 문의 게시판이나 이메일로 문의를 남겨주시면 순차적으로 답변드리겠습니다."),
            
            createFaq(FaqCategory.ETC, 2, 
                "제 작품을 판매하고 싶어요. 작가 신청은 어떻게 하나요?", 
                "뛰어난 크리에이터님을 언제나 환영합니다! 홈페이지 상단의 '작가 신청' 메뉴를 통해 포트폴리오와 함께 신청서를 제출해주시면, 내부 검토 후 개별적으로 연락드립니다."),
            
            createFaq(FaqCategory.ETC, 3, 
                "사업 제휴나 광고를 문의하고 싶습니다.", 
                "비즈니스 관련 문의는 이메일로 제안 내용을 보내주시면 담당자가 검토 후 회신 드리겠습니다."),
            
            createFaq(FaqCategory.ETC, 4, 
                "사이트 이용 중 오류가 발생했어요.", 
                "서비스 이용에 불편을 드려 죄송합니다. 문제 상황을 최대한 자세하게 기재하여 1:1 문의로 알려주시면 빠르게 확인하여 조치하겠습니다. (오류 메시지, 사용 중인 브라우저 등)")
        );
        
        faqRepository.saveAll(faqs);
        log.info("FAQ 초기 데이터 생성 완료: {}개", faqs.size());
    }
    
    private Faq createFaq(FaqCategory category, int sortOrder, String question, String answer) {
        Faq faq = new Faq();
        faq.setCategory(category);
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setSortOrder(sortOrder);
        faq.setIsActive(true);
        faq.setViewCount(0);
        return faq;
    }
} 