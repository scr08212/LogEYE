# LogEYE

[![CI Status](https://github.com/scr08212/logeye/actions/workflows/ci.yml/badge.svg)](https://github.com/scr08212/logeye/actions)

LogEYE는 여러 애플리케이션에서 발생하는 에러 및 로그 데이터를 실시간으로 수집하고, 지능적으로 그룹핑하여 개발팀에게 제공하는 '팀 단위 에러 추적 및 해결 워크플로우' 플랫폼입니다.  
개발팀은 LogEYE를 통해 분산된 에러 정보를 중앙에서 관리하고, 문제의 근본 원인을 신속하게 파악하여 해결 과정의 효율성을 극대화할 수 있습니다.  
## 2. 주요 기능 (Features)


- **멀티테넌시:** 사용자별, 프로젝트별 데이터 완벽 격리
- **데이터 수집:** 표준 API를 통한 실시간 에러 데이터 수집
- **지능형 이슈 그룹핑:** 동일한 에러를 자동으로 그룹핑하여 중복 알림 방지
- **이슈 관리:** 에러(이슈)의 상태(Unhandled, Resolved, Ignored) 관리 및 워크플로우 지원
- **AI 기반 원인 분석:** LLM을 활용한 에러의 근본 원인 및 해결책 제안 (Phase 3 구현 예정)

## 3. 기술 스택 (Tech Stack)

| Category | Technology |
|---|---|
| **Backend** | Spring Boot, Spring Security, Spring Data JPA |
| **Database**| PostgreSQL |
| **DevOps** | GitHub Actions, Docker, Docker Compose, Nginx |
| **Testing** | JUnit5, Mockito |

## 4. 아키텍처 (Architecture)

*(이 섹션은 Sprint 4가 끝날 무렵, 전체 시스템 구성이 확정되면 다이어그램으로 채워 넣는다.)*

## 5. ERD (Database Schema)

*(이 섹션은 Sprint 1의 첫 번째 이슈('Docs: 데이터베이스 ERD 설계...') 진행 시, 완성된 ERD 이미지를 여기에 추가한다.)*

![ERD](./docs/erd.png)