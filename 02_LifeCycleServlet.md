# LifeCycleServlet 설명서

이 설명서는 [LifeCycleServlet.java](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java) 코드의 서블릿 생명주기(Lifecycle) 원리를 초심자용 비유부터 중급자용 메커니즘, 그리고 기술 면접 대비 질문까지 단계별로 설명합니다.

---

## 01. 초심자용 실생활 비유 🏛️

서블릿의 생명주기(Lifecycle)는 **"놀이공원의 특정 어트랙션(놀이기구)의 하루 일과"**에 비유할 수 있습니다.

* **`init()` (기계 전원 켜기 및 안전 검사)**: 아침에 영업을 개시할 때 기계 장치를 켜고 준비 작업을 마치는 과정입니다. 매번 손님이 탑승할 때마다 전원을 껐다 켜는 것이 아니라, **최초로 탑승할 손님이 올 때 단 한 번만** 시동을 켜고 준비합니다. (손님이 아예 안 오면 전원을 켜지 않고 기다리는 지연 작동/Lazy방식입니다.)
* **`service()` (입구 안내 직원)**: 손님이 올 때마다 안내 직원이 맞이하며, 손님이 소지한 탑승권 종류(GET, POST 같은 HTTP 메서드)를 확인하고 알맞은 체험 코스로 안내해 주는 역할을 합니다.
* **`doGet()` (실제 어트랙션 이용 코스)**: 안내 직원이 탑승권을 확인한 뒤 "GET 방식 코스"로 들어가라고 안내하여, 손님이 실제 놀이기구를 즐기는 핵심 서비스 제공 과정입니다.
* **`destroy()` (폐장 시 기계 전원 내리기)**: 밤이 되어 놀이공원이 영업을 마치고 문을 닫을 때, 기계 전원을 끄고 잔여 데이터를 초기화하며 마감하는 과정입니다.
* **`@WebServlet({"/lifecycle", "/lifecycle2"})` (두 개의 진입 통로)**: 어트랙션으로 들어갈 수 있는 입구가 2개(`/lifecycle`과 `/lifecycle2`) 있는 것과 같습니다. 손님이 어느 문으로 들어오든 동일한 기계를 타고 동일한 직원의 안내를 받게 됩니다.

---

## 02. 중급자용 실제 동작 원리, 의존성 및 문법 특성 톱니바퀴 ⚙️

### A. 서블릿 생명주기의 흐름과 톰캣의 내부 제어
1. **서블릿 로딩 및 지연 초기화 (Lazy Initialization)**:
   * 톰캣(WAS) 기동 시점에 [LifeCycleServlet](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L13)의 인스턴스가 미리 생성되는 것이 아니라, 클라이언트가 매핑된 URL로 첫 요청을 보낼 때 인스턴스가 동적으로 생성되고 [init](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L16) 메서드가 실행됩니다.
   * 기동 시점에 즉시 초기화(Eager Initialization)하고 싶다면 `@WebServlet(value = "/lifecycle", loadOnStartup = 1)`처럼 `loadOnStartup` 옵션을 설정하면 됩니다.
2. **`init()` 메서드**:
   * 서블릿 인스턴스 생성 시 **최초 1회만** 실행됩니다.
   * 데이터베이스 커넥션 풀 구축, 설정 파일 로드 등 초기 비용이 크고 공통으로 사용할 리소스를 준비하는 용도로 구현합니다.
3. **`service(HttpServletRequest, HttpServletResponse)` 메서드**:
   * 클라이언트 요청이 들어올 때마다 호출되는 메서드입니다.
   * 내부 코드를 보면 [super.service(req, resp)](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L28)가 작성되어 있는데, 이는 `HttpServlet` 클래스에 내장된 기본 요청 라우팅 로직을 의미합니다. 이 호출을 지우면 HTTP 요청의 GET, POST 여부를 파악하여 [doGet](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L32) 등으로 분배해 주는 로직이 끊어지게 되므로 필수적으로 유지해야 합니다.
4. **`destroy()` 메서드**:
   * WAS가 정상 종료되거나 애플리케이션이 리로드될 때 서블릿 인스턴스가 소멸하며 **마지막 1회** 호출됩니다.
   * `init()`에서 확보해 두었던 외부 자원(DB 연결, 소켓, 파일 스트림 등)을 안전하게 닫아주는 해제 작업을 수행합니다.

### B. 의존성 및 매핑 (Dependencies & Mapping)
* **다중 URL 매핑 (Multi-URL Mapping)**:
  * `@WebServlet({"/lifecycle", "/lifecycle2"})`처럼 중괄호 `{}` 배열 형식을 사용하여 하나의 서블릿에 여러 개의 URL 경로를 매핑할 수 있습니다. 이를 통해 공통 컨트롤러 로직을 단일 서블릿에 바인딩하여 관리 효율성을 높입니다.

### C. 자바 문법 특성
* **메서드 오버라이딩 (Overriding)**:
  * 부모인 `HttpServlet` 추상 클래스의 핵심 라이프사이클 훅([init](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L16), [service](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L24), [doGet](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L32), [destroy](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L40))을 자식 클래스에서 목적에 맞게 재정의하고 있습니다.
  * `init()`과 `destroy()` 메서드는 오버라이딩 시 부모의 `super.init()` 또는 `super.destroy()` 호출을 지워도 무방하지만, [service](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L24) 메서드는 부모의 위임 처리가 필요하므로 [super.service](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/LifeCycleServlet.java#L28) 호출이 유지되어야 합니다.

---

## 03. 면접 준비를 위한 예상 질문 & 모범 답안 🎤

### Q01. 서블릿 생명주기(Lifecycle) 메서드 종류와 호출 순서, 빈도에 대해 설명해 주세요.
> **모범 답안:**
> 서블릿의 생명주기는 크게 세 단계로 제어됩니다.
> 1. **`init()`**: 최초 요청 시 서블릿 인스턴스가 힙 영역에 생성된 직후 **최초 1회** 실행됩니다.
> 2. **`service()`**: 클라이언트의 HTTP 요청이 올 때마다 **매번** 호출되며, 내부적으로 `super.service()`를 통해 HTTP 요청 메서드를 감지하고 `doGet()`, `doPost()` 등으로 제어권을 분배합니다.
> 3. **`destroy()`**: 컨테이너가 서블릿을 더 이상 서비스하지 않고 소멸시키기로 결정했을 때(서버 종료 등) **최종 1회** 호출되어 자원을 해제합니다.

### Q02. 서블릿의 `service()` 메서드를 재정의하면서 `super.service(req, resp)` 호출을 빼버리면 어떤 현상이 발생하나요?
> **모범 답안:**
> `HttpServlet` 클래스에 정의된 `service()` 메서드는 HTTP 요청 타입(GET, POST, PUT, DELETE 등)에 따라 내부적으로 알맞은 `doGet()`, `doPost()` 등의 메서드를 식별하고 호출해 주는 분기 라우팅 매커니즘을 내장하고 있습니다.
> 따라서 `super.service(req, resp)`의 호출을 누락하게 되면 이 내부 라우팅 로직이 동작하지 않아, 우리가 정의한 `doGet()` 등의 메서드가 전혀 실행되지 않고 응답이 정상적으로 전달되지 않는 오동작이 발생합니다.

### Q03. 서블릿의 지연 초기화(Lazy Initialization)의 장점과, 이를 즉시 초기화(Eager Initialization)로 전환하는 방법을 설명해 주세요.
> **모범 답안:**
> 지연 초기화는 WAS가 시작될 때 사용되지 않을 수도 있는 모든 서블릿을 한 번에 생성하지 않고, 실제로 첫 번째 호출이 올 때 해당 서블릿의 생성과 `init()` 초기화를 실행함으로써 서버 기동 시간과 초기 메모리 점유율을 최적화할 수 있다는 장점이 있습니다.
> 만약 서버 실행 직후 즉시 모든 준비를 마쳐서 첫 접속자의 응답 지연을 방지하고자 한다면, `@WebServlet` 애노테이션 안에 `loadOnStartup = 1` 속성을 지정하거나 `web.xml` 설정에 `<load-on-startup>1</load-on-startup>`을 등록해 줌으로써 기동 시 즉시 인스턴스를 사전 생성하도록 지시할 수 있습니다.
