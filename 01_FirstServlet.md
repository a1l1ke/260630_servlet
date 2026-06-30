# FirstServlet 설명서

이 설명서는 [FirstServlet.java](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/FirstServlet.java) 코드의 역할과 원리를 초심자용 비유부터 중급자용 메커니즘, 그리고 기술 면접 대비 질문까지 단계별로 설명합니다.

---

## 01. 초심자용 실생활 비유 🏛️

서블릿(Servlet)은 웹 서버라는 **"관공서(또는 매장)"**에서 특정 업무를 처리하는 **"담당 창구 직원"**에 비유할 수 있습니다.

* **브라우저 (고객)**: 서비스를 이용하기 위해 서버에 요청을 보내는 손님입니다.
* **URL 요청 (번호표/안내판)**: 고객이 `http://localhost:8080/first`라는 번호표를 뽑고 해당 창구로 찾아갑니다.
* **`@WebServlet("/first")` (창구 번호판)**: [FirstServlet](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/FirstServlet.java#L21) 클래스 위에 붙은 이 표식은 **"여기가 '/first' 업무를 담당하는 1번 창구입니다"**라고 문 앞에 걸어둔 안내판과 같습니다.
* **`HttpServletRequest` (신청서)**: 고객이 작성해 제출한 신청서입니다. 여기에는 고객의 신원 정보, 요청 사항(파라미터), 브라우저 정보 등이 적혀 있습니다.
* **`HttpServletResponse` (처리 결과 쟁반)**: 직원이 신청을 처리한 후 고객에게 돌려줄 결과물 쟁반입니다. 이 쟁반 위에 HTML 태그나 텍스트 같은 데이터를 담아 고객에게 돌려보냅니다.
* **`doGet` 메서드 (업무 매뉴얼)**: 고객이 단순 조회 목적(GET 방식)으로 창구를 찾았을 때, 직원이 수행해야 하는 구체적인 행동 매뉴얼입니다.

---

## 02. 중급자용 실제 동작 원리, 의존성 및 문법 특성 톱니바퀴 ⚙️

### A. 서블릿의 실제 동작 메커니즘과 생명주기 (Life Cycle)
* **01. HTTP 요청 수신 및 객체 생성**: 클라이언트의 HTTP 요청이 WAS(Tomcat 등)로 들어오면, WAS는 HTTP 요청 메시지를 파싱하여 `HttpServletRequest`와 `HttpServletResponse` 객체를 힙 메모리에 생성합니다.
* **02. 서블릿 인스턴스 조회 및 생성 (싱글톤)**: 
   * WAS는 설정 정보(애노테이션 `@WebServlet` 또는 `web.xml`)를 참조하여 요청된 URL 패턴(`/first`)이 어떤 서블릿에 매핑되어 있는지 찾습니다.
   * 해당 서블릿의 인스턴스가 메모리에 없다면 최초 1회 인스턴스를 생성하고 초기화 메서드인 `init()`을 호출합니다. 이후 요청부터는 이미 메모리에 로드된 **싱글톤(Singleton)** 인스턴스를 재사용합니다.
* **03. 스레드 할당 및 서비스 실행**:
   * 각 요청에 대해 WAS의 스레드 풀(Thread Pool)에서 작업 스레드를 하나 할당합니다.
   * 이 스레드가 서블릿 인스턴스의 `service()` 메서드를 실행하고, `service()` 내에서 HTTP 메서드(GET)에 맞는 [doGet](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/FirstServlet.java#L23) 메서드를 오버라이딩된 본문으로 호출합니다.
* **04. 응답 및 소멸**:
   * [doGet](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/FirstServlet.java#L23) 내에서 `resp.getWriter().println()` 등을 통해 응답 버퍼에 데이터를 작성합니다.
   * 메서드 실행이 끝나면 WAS는 응답 객체의 내용을 HTTP Response 패킷으로 패키징하여 브라우저에 전송하고, 생성했던 `HttpServletRequest`와 `HttpServletResponse` 객체를 소멸(가비지 컬렉션 대상)시킵니다.

### B. 의존성 (Dependencies)
* **01. Jakarta EE API 의존성**:
  * `jakarta.servlet.http.HttpServlet`, `jakarta.servlet.http.HttpServletRequest` 등을 사용하기 위해 `jakarta.servlet-api` 라이브러리가 필요합니다.
  * 과거 Java EE 시절에는 `javax.servlet` 패키지를 사용했으나, 상표권 이관 문제로 인해 Jakarta EE 9 버전부터는 `jakarta.servlet` 패키지명으로 네임스페이스가 일괄 변경되었습니다. 따라서 사용하는 WAS 버전(Tomcat 10 이상 등)에 맞는 패키지를 임포트해야 정상 작동합니다.

### C. 자바 문법 특성
* **01. 추상 클래스 상속 및 메서드 오버라이딩**: [FirstServlet](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/FirstServlet.java#L21)은 `HttpServlet` 추상 클래스를 상속(extends)받고 있으며, 부모 클래스가 정의한 `doGet` 메서드를 재정의(`@Override`)하고 있습니다. 이를 통해 서블릿 컨테이너가 약속된 프로토콜에 따라 해당 메서드를 다형성(Polymorphism)을 활용해 호출할 수 있게 됩니다.
* **02. 텍스트 블록 (Text Blocks - Java 15+)**: [FirstServlet.java:L31-34](file:///Users/morgan/Documents/workspace/servlet/src/main/java/com/example/servlet/FirstServlet.java#L31-L34)에서 `"""` 문법을 사용하고 있습니다. 이를 통해 여러 줄에 걸친 HTML 코드 문자열을 줄바꿈 문자(`\n`)나 문자열 더하기(`+`) 없이 편리하게 선언하여 가독성을 크게 높였습니다.

---

## 03. 면접 준비를 위한 예상 질문 & 모범 답안 🎤

### Q01. 서블릿(Servlet)의 정의와 생명주기(Life Cycle)에 대해 설명해 주세요.
> **모범 답안:**
> 서블릿은 클라이언트의 요청에 대해 동적으로 서비스를 제공하는 자바 기반의 웹 컴포넌트입니다. 서블릿의 생명주기는 서블릿 컨테이너(WAS)에 의해 제어되며, 크게 세 단계로 나뉩니다.
> 1. **초기화 (`init()`)**: 클라이언트의 최초 요청 시 서블릿 클래스가 로딩되고 인스턴스가 생성된 직후 호출되어 초기화 작업을 수행합니다.
> 2. **요청 처리 (`service()`)**: 요청이 들어올 때마다 호출되며, 클라이언트의 HTTP Method(GET, POST 등)를 분석하여 `doGet()`, `doPost()` 등 적절한 메서드로 위임합니다.
> 3. **소멸 (`destroy()`)**: 서블릿 컨테이너가 종료되거나 서블릿이 언로드될 때 호출되어 자원을 해제합니다.

### Q02. 서블릿은 멀티스레드 환경에서 어떻게 동작하며, 개발 시 어떤 점을 주의해야 하나요?
> **모범 답안:**
> 서블릿 컨테이너는 메모리 절약과 성능 향상을 위해 각 서블릿을 **싱글톤(Singleton) 패턴**으로 단 하나만 생성하고 관리합니다. 클라이언트의 요청이 들어오면 WAS의 스레드 풀에서 각각의 스레드를 할당해 하나의 서블릿 인스턴스 내 `service()` 메서드를 동시 실행시킵니다.
> 따라서 **스레드 안전성(Thread-Safe)**을 확보하는 것이 매우 중요합니다. 서블릿 클래스의 멤버 변수(필드)는 모든 스레드가 공유하므로 가변(Mutable) 상태값을 필드에 저장하면 동시성 문제가 발생합니다. 상태 정보는 항상 개별 스레드의 스택 영역에 할당되는 **지역 변수**로 선언하여 사용하거나, `HttpServletRequest` 등 스레드별로 독립적인 객체에 저장해 사용해야 합니다.

### Q03. `javax.servlet`과 `jakarta.servlet` 패키지의 차이와 네임스페이스 변경 배경을 아시나요?
> **모범 답안:**
> Oracle이 Java EE 기술을 Eclipse Foundation으로 기부하면서 'Java' 상표권 사용에 제약이 생겨 명칭을 Jakarta EE로 변경하게 되었습니다. 이 과정에서 Jakarta EE 9 버전부터 모든 표준 스펙의 패키지명이 `javax.*`에서 `jakarta.*`로 마이그레이션되었습니다.
> 따라서 Tomcat 9 이하의 WAS에서는 기존 `javax.servlet` 기반 애플리케이션이 구동되지만, Tomcat 10 이상의 최신 WAS에서는 `jakarta.servlet` 패키지를 사용한 서블릿만 정상적으로 로드되고 인식됩니다.

### Q04. `@WebServlet` 애노테이션의 장점은 무엇이며, 설정 방식의 역사적 변화는 어떠한가요?
> **모범 답안:**
> 서블릿 2.x 버전까지는 새로운 서블릿을 등록하기 위해 XML 설정 파일인 `web.xml`에 `<servlet>`과 `<servlet-mapping>` 태그를 일일이 지정해 주어야 했습니다. 이는 서블릿이 많아질수록 XML 파일이 비대해지고 오타로 인한 런타임 에러가 발생하기 쉽다는 단점이 있었습니다.
> 서블릿 3.0 스펙부터 도입된 `@WebServlet` 애노테이션 덕분에 XML 설정 없이 자바 클래스 선언부 위에 애노테이션을 작성하는 것만으로 간단하게 URL 매핑을 수행할 수 있게 되었으며, 코드의 응집성과 가유지보수성이 크게 향상되었습니다.
