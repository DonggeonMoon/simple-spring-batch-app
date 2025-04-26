# 📄 simple-spring-batch-app

## ✨ 프로젝트 소개

Spring Batch 예제  
Spring Batch와 Spring Scheduler를 이용해 **간단한 배치 작업**을 구성한 예제이다.  
주가 API를 호출해 10개 종목의 주가 정보를 가져오고, 파일에 저장하는 기능을 수행한다.

## 🛠️ 사용 기술

- Java 17
- Spring Boot 3.4.5
  (Spring Batch 5.2.2, Spring Context 6.2.6 기반)

## 📦 Batch Job 구성

### Step: Tasklet 방식

- **Tasklet** 기반으로 주가 데이터를 조회하고 파일로 저장하는 단일 작업을 수행한다.

#### Tasklet 방식을 사용한 이유

- 1회성 작업에는 Chunk 기반 처리가 적합하지 않다.
- 반복적이고 대량 데이터 처리가 필요한 경우에만 Chunk가 효과적이다.

#### 작업 내용

- 주가 API를 통해 10개 종목의 시세 데이터를 조회한다.
- 조회한 데이터를 파일로 저장한다.

## ⏰ Scheduler 구성

- 평일(월~금) 9시부터 17시까지, 30초마다 Batch Job을 실행한다.
- 크론 표현식:

```java
@Scheduled(cron = "*/30 * 9-17 * * 1-5")