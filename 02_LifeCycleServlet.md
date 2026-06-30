# LifeCycleServlet 설명서

이 설명서는 [LifeCycleServlet.java](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java) 코드의 서블릿 생명주기(Lifecycle) 및 멀티스레드 환경의 상태 공유(Race Condition) 원리를 초심자용 비유부터 중급자용 메커니즘, 그리고 기술 면접 대비 질문까지 단계별로 설명합니다.

---

## 01. 초심자용 실생활 비유 🏛️

### A. 서블릿 생명주기 비유
서블릿의 생명주기는 **"놀이공원의 특정 어트랙션(놀이기구)의 하루 일과"**에 비유할 수 있습니다.

* **`init()` (기계 전원 켜기 및 안전 검사)**: 아침에 영업을 개시할 때 기계 장치를 켜고 준비 작업을 마치는 과정입니다. 매번 손님이 탑승할 때마다 전원을 껐다 켜는 것이 아니라, **최초로 탑승할 손님이 올 때 단 한 번만** 시동을 켜고 준비합니다. (손님이 아예 안 오면 전원을 켜지 않고 기다리는 지연 작동/Lazy방식입니다.)
* **`service()` (입구 안내 직원)**: 손님이 올 때마다 안내 직원이 맞이하며, 손님이 소지한 탑승권 종류(GET, POST 같은 HTTP 메서드)를 확인하고 알맞은 체험 코스로 안내해 주는 역할을 합니다.
* **`doGet()` (실제 어트랙션 이용 코스)**: 안내 직원이 탑승권을 확인한 뒤 "GET 방식 코스"로 들어가라고 안내하여, 손님이 실제 놀이기구를 즐기는 핵심 서비스 제공 과정입니다.
* **`destroy()` (폐장 시 기계 전원 내리기)**: 밤이 되어 놀이공원이 영업을 마치고 문을 닫을 때, 기계 전원을 끄고 잔여 데이터를 초기화하며 마감하는 과정입니다.
* **`@WebServlet({"/lifecycle", "/lifecycle2"})` (두 개의 진입 통로)**: 어트랙션으로 들어갈 수 있는 입구가 2개(`/lifecycle`과 `/lifecycle2`) 있는 것과 같습니다. 손님이 어느 문으로 들어오든 동일한 기계를 타고 동일한 직원의 안내를 받게 됩니다.

### B. 멤버 변수 `count`와 경쟁 상태(Race Condition) 비유
* **`count` 변수 (공동 누적 기록 공책)**: 1번 창구(싱글톤 서블릿)에 단 한 권만 존재하는 **"누적 방문자 기록 공책"**입니다.
* **지연 루프 (졸며 기록하는 작업)**: 손님이 올 때마다 직원들은 공책에 `+1`을 3,000번 반복해서 적어야 합니다. 그런데 이 작업이 너무 느려서(`Thread.sleep(1)`), 펜을 쥔 채 꾸벅꾸벅 졸아가며 한 줄 한 줄 기록합니다.
* **경쟁 상태 (동시 낙서 대참사)**: 
  * A 창구 직원이 공책을 가져와 "현재 100명이네, 3,000번 더해서 3,100으로 적어야지" 하고 졸면서 연필로 슥슥 쓰고 있습니다.
  * 그 사이 B 창구 직원이 들어와 똑같이 공책을 보고 "어? 아직 100명이네. 나도 3,000번 더해서 3,100으로 적어야지" 하고 졸면서 씁니다.
  * 결국 두 직원이 동시에 같은 공책 페이지에 난타하듯 덮어쓰기를 해버려, 두 명이 각각 3,000번씩 총 6,000번을 더했음에도 결과는 6,100이 아닌 엉뚱한 값(예: 3,200 등)으로 뭉개지고 맙니다. 이것이 바로 여러 작업이 동시에 한 자원에 접근하여 결과를 망가뜨리는 **경쟁 상태(Race Condition)**입니다.

---

## 02. 중급자용 실제 동작 원리, 의존성 및 문법 특성 톱니바퀴 ⚙️

### A. 서블릿 생명주기와 톰캣의 내부 제어
1. **서블릿 로딩 및 지연 초기화 (Lazy Initialization)**:
   * 톰캣(WAS) 기동 시점에 [LifeCycleServlet](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L13)의 인스턴스가 미리 생성되는 것이 아니라, 클라이언트가 매핑된 URL로 첫 요청을 보낼 때 인스턴스가 동적으로 생성되고 [init](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L19) 메서드가 실행됩니다.
   * 기동 시점에 즉시 초기화(Eager Initialization)하고 싶다면 `@WebServlet(value = "/lifecycle", loadOnStartup = 1)`처럼 `loadOnStartup` 옵션을 설정하면 됩니다.
2. **`init()` 메서드**:
   * 서블릿 인스턴스 생성 시 **최초 1회만** 실행됩니다.
   * 데이터베이스 커넥션 풀 구축, 설정 파일 로드 등 초기 비용이 크고 공통으로 사용할 리소스를 준비하는 용도로 구현합니다.
3. **`service(HttpServletRequest, HttpServletResponse)` 메서드**:
   * 클라이언트 요청이 들어올 때마다 호출되는 메서드입니다.
   * 내부 코드를 보면 [super.service(req, resp)](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L31)가 작성되어 있는데, 이는 `HttpServlet` 클래스에 내장된 기본 요청 라우팅 로직을 의미합니다. 이 호출을 지우면 HTTP 요청의 GET, POST 여부를 파악하여 [doGet](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L35) 등으로 분배해 주는 로직이 끊어지게 되므로 필수적으로 유지해야 합니다.
4. **`destroy()` 메서드**:
   * WAS가 정상 종료되거나 애플리케이션이 리로드될 때 서블릿 인스턴스가 소멸하며 **마지막 1회** 호출됩니다.
   * `init()`에서 확보해 두었던 외부 자원(DB 연결, 소켓, 파일 스트림 등)을 안전하게 닫아주는 해제 작업을 수행합니다.

### B. 싱글톤 서블릿의 상태 공유와 경쟁 상태 (Race Condition)
* **싱글톤 상태 공유**:
  * [LifeCycleServlet](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L13)은 서블릿 컨테이너 내에 단 **하나의 인스턴스(Singleton)**로만 존재합니다.
  * 멤버 변수인 [count](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L16)는 인스턴스 필드이므로 메모리 상에 단 하나만 존재하며, 여러 요청 스레드들이 이 변수를 공유합니다.
* **경쟁 상태 유발 메커니즘**:
  * [doGet](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L35) 메서드 내부에서 3,000번 루프를 돌며 `count++`를 수행합니다.
  * 루프 내에 `Thread.sleep(1)`을 주어 강제로 스레드가 잠들게 만듦으로써 CPU 제어권을 다른 스레드에 넘겨주는 **컨텍스트 스위칭(Context Switching)**이 극도로 자주 발생하도록 유도했습니다.
  * 이로 인해 한 스레드가 `count` 값을 읽어 연산한 후 메모리에 다시 쓰기 전에 다른 스레드가 개입하여 옛날 `count` 값을 읽어가 연산함으로써 값이 덮어써지게(Lost Update) 됩니다.
  * 그 결과, 스레드 안전성이 완전히 깨지고 최종 출력되는 [count](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L16) 값이 실제 누적 요청 횟수와 일치하지 않게 됩니다.

### C. 의존성 및 문법 특성
* **다중 URL 매핑 (Multi-URL Mapping)**:
  * `@WebServlet({"/lifecycle", "/lifecycle2"})`처럼 중괄호 `{}` 배열 형식을 사용하여 하나의 서블릿에 여러 개의 URL 경로를 매핑할 수 있습니다.
* **`super.service` 명시**:
  * 오버라이딩된 `service` 메서드에서 `super.service()`를 재호출하여 서블릿 표준 규격에 부합하게 동작하도록 제어권을 위임합니다.

---

## 03. 면접 준비를 위한 예상 질문 & 모범 답안 🎤

### Q01. 서블릿 생명주기(Lifecycle) 메서드 종류와 호출 순서, 빈도에 대해 설명해 주세요.
> **모범 답안:**
> 서블릿의 생명주기는 크게 세 단계로 제어됩니다.
> 1. **`init()`**: 최초 요청 시 서블릿 인스턴스가 힙 영역에 생성된 직후 **최초 1회** 실행됩니다.
> 2. **`service()`**: 클라이언트의 HTTP 요청이 올 때마다 **매번** 호출되며, 내부적으로 `super.service()`를 통해 HTTP 요청 메서드를 감지하고 `doGet()`, `doPost()` 등으로 제어권을 분배합니다.
> 3. **`destroy()`**: 컨테이너가 서블릿을 더 이상 서비스하지 않고 소멸시키기로 결정했을 때(서버 종료 등) **최종 1회** 호출되어 자원을 해제합니다.

### Q02. 서블릿은 멀티스레드 환경에서 싱글톤으로 동작합니다. 이때 발생할 수 있는 동시성 이슈(Thread-Safety)와 주의할 점은 무엇인가요?
> **모범 답안:**
> 서블릿 컨테이너는 동일한 서블릿 객체를 **싱글톤(Singleton)**으로 단 하나만 메모리에 적재하고 공유합니다. 클라이언트의 다중 요청은 WAS의 별도 스레드들을 통해 동시에 이 서블릿 인스턴스에 접근합니다.
> 따라서 서블릿 클래스 내부의 **멤버 변수(인스턴스 필드)**에 가변적인 데이터(상태)를 저장하게 되면 여러 스레드가 동시에 이를 수정하면서 데이터 정합성이 깨지는 **동시성 문제(Race Condition)**가 발생합니다. 이를 방지하기 위해 서블릿은 가변 멤버 변수를 갖지 않도록 **무상태(Stateless)**로 설계해야 하며, 모든 비즈니스 데이터는 메서드 내의 **지역 변수(스택 영역)**로 처리하여 독립성을 보장해야 합니다.

### Q03. 서블릿의 `service()` 메서드를 재정의하면서 `super.service(req, resp)` 호출을 빼버리면 어떤 현상이 발생하나요?
> **모범 답안:**
> `HttpServlet` 클래스에 정의된 `service()` 메서드는 HTTP 요청의 메서드(GET, POST, PUT, DELETE 등)를 식별하고 그에 상응하는 `doGet()`, `doPost()` 등의 메서드로 흐름을 라우팅해주는 기본 역할을 수행합니다.
> 따라서 `super.service(req, resp)`의 호출을 누락하게 되면 이 내부 라우팅 로직이 동작하지 않아, 우리가 정의한 `doGet()` 등의 메서드가 전혀 실행되지 않고 응답이 정상적으로 전달되지 않는 오동작이 발생합니다.

### Q04. 서블릿의 지연 초기화(Lazy Initialization)의 장점과, 이를 즉시 초기화(Eager Initialization)로 전환하는 방법을 설명해 주세요.
> **모범 답안:**
> 지연 초기화는 WAS가 시작될 때 사용되지 않을 수도 있는 모든 서블릿을 한 번에 생성하지 않고, 실제로 첫 번째 호출이 올 때 해당 서블릿의 생성과 `init()` 초기화를 실행함으로써 서버 기동 시간과 초기 메모리 점유율을 최적화할 수 있다는 장점이 있습니다.
> 만약 서버 실행 직후 즉시 모든 준비를 마쳐서 첫 접속자의 응답 지연을 방지하고자 한다면, `@WebServlet` 애노테이션 안에 `loadOnStartup = 1` 속성을 지정하거나 `web.xml` 설정에 `<load-on-startup>1</load-on-startup>`을 등록해 줌으로써 기동 시 즉시 인스턴스를 사전 생성하도록 지시할 수 있습니다.

### Q05. `LifeCycleServlet`에서 멤버 변수 `count`의 동시성 이슈를 극복하는 구체적인 대안은 무엇인가요?
> **모범 답안:**
> 가장 좋은 방법은 서블릿 필드에 상태값을 저장하지 않고 비즈니스 서비스나 DB 레코드로 관리하는 것이지만, 자바 서블릿 레벨에서 상태 저장을 강제해야 한다면 다음의 대안이 있습니다.
> 1. **`synchronized` 블록 사용**: 메서드 전체 혹은 필드 접근 연산 부분을 `synchronized(this)` 등으로 임계 영역 지정합니다. 단, 스레드가 병목을 겪어 전체 성능이 저하될 수 있습니다.
> 2. **`Atomic` 타입 변수 사용**: `int` 대신 `AtomicInteger` 객체를 선언하여 `incrementAndGet()` 메서드를 호출합니다. CAS(Compare-And-Swap) 알고리즘을 사용하므로 동기화 락 없이 원자성을 보장해 줍니다.
