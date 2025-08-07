# Awesome README Template

<!--배지-->
![MIT License][license-shield] ![Java][spring-shield]

# Table of Contents
- [[1] Project Overview](#1-project-overview)
  - [Key Features](#key-features)
- [[2] Technical Overview](#2-technical-overview)
  - [Tech Stack](#tech-stack)
  - [Architecture](#architecture)
- [[3] Getting Started](#3-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Configuration](#configuration)
  - [Running the App](#running-the-app)
- [[4] User Guide](#4-user-guide)
  - [Documentation](#documentation)
  - [Examples](#examples)
- [[5] Trouble Shooting](#5-trouble-shooting)
- [[6] Roadmap](#6-roadmap)
- [[7] Contribution](#7-contribution)
- [[8] Acknowledgement](#8-acknowledgement)
- [[9] Contact](#9-contact)
- [[10] License](#10-license)

# [1] Project Overview
숲 인터네셔널의 B/L 넘버 추출 및 공공 API를 활용한 자동 엑셀 파일 완성 프로젝트

## Key Features
- 📄 **OCR**: 구글 비전 API를 활용한 OCR을 통해, 원하는 정보를 추출 및 가공합니다.

# [2] Technical Overview

## Tech Stack
- [Maven](https://maven.apache.org/) 3.6.3
- [MySQL](https://www.mysql.com/) 8.0
- [Spring](https://spring.io/) 2.4.3

## Architecture
![architecture](doc/svgs/architecture.drawio.svg)


# [3] Getting Started
> 💡 운영체제에 따라 동작 방식이 다르다면 OS별 실행 방법도 함께 설명해주세요.
## Prerequisites
> 💡 프로젝트 실행에 필요한 도구와 라이브러리를 나열하고 설치 방법을 간단히 안내하세요.
- [OpenWeather API key](https://openweathermap.org/) for free
- npm
```bash
npm install npm@latest -g
```

## Installation
> 💡 프로젝트 소스를 받는 방법과 초기 설정 과정을 설명하세요.
1. Repository 클론
```bash
git clone https://github.com/your-username/project-repository
```
2. NPM 패키지 설치
```bash
npm install
```

## Configuration
> 💡 필수 설정 값이 있다면 어디에 어떤 값을 입력해야 하는지 설명하세요.
- `config.js` 파일 내 OpenWeather API 키 입력
```bash
const API_KEY = "<Your API key>";
```

## Running the App
> 💡 로컬 환경에서 애플리케이션을 실행하는 방법을 설명하세요.
```bash
npm run start
```


# [4] User Guide
> 💡 프로젝트에 따라 다음과 같은 섹션도 포함해보세요: 통합 가이드, API 사용법, 테스트 방법, 고급 설정
## Documentation
> 💡 별도의 문서가 있다면 여기에 링크를 첨부하세요.
- 📔 [API Documentation](https://devyoujin.github.io) 

## Examples
![usage](doc/images/usage.png)

```go
// API 사용 예제 코드
```


# [5] Trouble Shooting
> 💡 자주 발생하는 문제(FAQ)와 해결 방법을 안내하고, FAQ에 없는 경우 도움을 요청하는 방법도 함께 설명하세요.
문제 해결에 대한 정보가 부족하다면, 언제든지 [GitHub Issues][trouble-shooting-url]에 이슈를 등록해주세요.

**Error: API Key Missing**
- 원인: 필수 API 키가 설정되지 않았거나, 유효하지 않은 경우 발생
- 해결: `config.js` 파일에서 `API_KEY` 값을 올바르게 설정했는지 확인하고, 키가 유효한지 다시 확인한다.


# [6] Roadmap
|Milestone|Estimated Timeline|Details|
|---|---|---|
|v1.1.0|May 2025|사용자 피드백 기반 UI/UX 개선|
|v2.0.0|June 2025|결제 서비스 연동|
|v2.1.0|July 2025|모바일 최적화|


# [7] Contribution
> 💡 Emoji guide: ✨(새로운 기능)
🐞(버그 수정), 📄(문서 개선), 🔨(리팩토링), ⚡️(성능 향상), 🔒(보안 이슈 해결), 💡(제안/아이디어)

프로젝트에 대한 모든 기여는 언제나 환영합니다. 자세한 기여 방법은 [`contributing guide`][contribution-url]를 참고해 주세요.

프로젝트를 개선해주신 모든 기여자 분들께 진심으로 감사드립니다! 🙌
- 🐞 [YOUJIN LEE(devyoujin)](https://github.com/devyoujin): 메인 페이지 버그 수정


# [8] Acknowledgement
> 💡 프로젝트에 영감을 준 블로그나 레포지토리, 사용한 도구 등을 자유롭게 링크로 첨부하세요.
- [Readme Template - Embedded Artistry](https://embeddedartistry.com/blog/2017/11/30/embedded-artistry-readme-template/)
- [How to write a kickass Readme - James.Scott](https://dev.to/scottydocs/how-to-write-a-kickass-readme-5af9)
- [Best-README-Template - othneildrew](https://github.com/othneildrew/Best-README-Template#prerequisites)
- [Img Shields](https://shields.io/)
- [Github Pages](https://pages.github.com/)
- [Draw.io](https://app.diagrams.net/)


# [9] Contact
- 📧 youjin.lee.dev@gmail.com
- 📋 [Contact Form](https://devyoujin.github.io/contact)


# [10] License
이 프로젝트는 MIT 라이선스를 따릅니다. 자세한 내용은 [LICENSE][license-url] 파일을 참고해주세요.


<!--URL for Badges-->
[license-shield]: https://img.shields.io/github/license/devyoujin/awesome-readme-template?labelColor=D8D8D8&color=04B4AE
[spring-shield]: https://img.shields.io/github/spring/devyoujin/awesome-readme-template?labelColor=D8D8D8&color=BE81F7
[google-vision-shield]: https://img.shields.io/github/issues-closed/devyoujin/awesome-readme-template?labelColor=D8D8D8&color=FE9A2E

<!--URL for Buttons-->
[readme-en-shield]: https://img.shields.io/badge/-readme%20in%20english-2E2E2E?style=for-the-badge
[view-demo-shield]: https://img.shields.io/badge/-%F0%9F%98%8E%20view%20demo-F3F781?style=for-the-badge
[view-demo-url]: https://devyoujin.github.io
[report-bug-shield]: https://img.shields.io/badge/-%F0%9F%90%9E%20report%20bug-F5A9A9?style=for-the-badge
[report-bug-url]: https://github.com/devyoujin/awesome-readme-template/issues
[request-feature-shield]: https://img.shields.io/badge/-%E2%9C%A8%20request%20feature-A9D0F5?style=for-the-badge
[request-feature-url]: https://github.com/devyoujin/awesome-readme-template/issues

<!--URL for Links-->
[trouble-shooting-url]: https://github.com/devyoujin/awesome-readme-template/issues
[license-url]: LICENSE.md
[contribution-url]: CONTRIBUTION.md
[readme-en-url]: README.md
