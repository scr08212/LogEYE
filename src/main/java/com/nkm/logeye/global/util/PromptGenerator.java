package com.nkm.logeye.global.util;

import com.nkm.logeye.domain.issue.Issue;
import com.nkm.logeye.domain.issue.IssueEvent;
import org.springframework.stereotype.Component;

@Component
public final class PromptGenerator {

    private static final String SYSTEM_PROMPT = """
            당신은 20년 경력의 시니어 백엔드 개발자입니다.
            제공되는 에러 Message, StackTrace, ContextData를 바탕으로 문제의 원인과 해결책을 제시하세요.

            답변은 다음 JSON 형식으로만 응답해야 합니다.
            {
              "estimated_cause": "여기에 원인을 분석하여 작성합니다.",
              "solution_suggestion": "여기에 구체적인 해결책을 작성합니다."
            }
            """;

    public String generate(Issue issue, IssueEvent issueEvent){

        String body = "##Message\n"+issue.getMessage()+ '\n';
        body += "##StackTrace\n"+issue.getStackTrace()+ '\n';
        body += "##ContextData\n"+issueEvent.getContextData()+ '\n';

        return SYSTEM_PROMPT + body;
    }
}