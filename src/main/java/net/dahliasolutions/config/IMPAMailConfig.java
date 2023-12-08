package net.dahliasolutions.config;

//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.integration.config.EnableIntegration;
//import org.springframework.integration.dsl.IntegrationFlow;
//import org.springframework.integration.dsl.MessageChannels;
//import org.springframework.integration.mail.dsl.Mail;
//import org.springframework.integration.mail.support.DefaultMailHeaderMapper;
//import org.springframework.integration.mapping.HeaderMapper;
//
//@Configuration
//@EnableIntegration
//@RequiredArgsConstructor
//public class IMPAMailConfig {
//
//
//    @Bean
//    public IntegrationFlow imapFlow() {
//        return IntegrationFlow
//                .from(Mail.imapInboundAdapter("imap://crew%40destinyworship.com:Worship1##@Outlook.office365.com:993/INBOX")
//                                .userFlag("testSIUserFlag")
//                                .simpleContent(true)
//                                .javaMailProperties(p -> p.put("mail.debug", "false"))
//                                .headerMapper(mailHeaderMapper()))
//                .handle(System.out::println)
////                .handle(parseNewMail)
//                .get();
//    }
//
//    @Bean
//    public HeaderMapper<MimeMessage> mailHeaderMapper() {
//        return new DefaultMailHeaderMapper();
//    }
//
//}
