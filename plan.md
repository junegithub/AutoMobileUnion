# iOS Native Migration Plan

## Goal

Build a new iOS native app for the current `AutoMobileUnion` Android project.

Primary goal:
- get an iOS version online with core business available

Secondary goal:
- keep scope controlled to reduce iteration cost and token usage

Non-goal:
- do not try to fully redesign the product before parity
- do not rewrite Android during the iOS effort
- do not start with Flutter for this phase

## Source Of Truth

Use the current Android project in this repo as the main source of truth.

Key reference areas:
- `app/src/main/java/com/fx/zfcar/car`
- `app/src/main/java/com/fx/zfcar/training`
- `app/src/main/java/com/fx/zfcar/net`
- `app/src/main/res/layout`

Extra visual and interaction reference:
- `/Users/source/ytcar-app`

When Android behavior and `ytcar-app` differ:
1. prefer current Android business logic
2. use `ytcar-app` for UI style and interaction refinement
3. document any intentional deviation

## Delivery Strategy

Use staged native iOS delivery.

Reason:
- current project has heavy platform dependencies: AMap, video playback, WebView bridge, WeChat/Alipay, CameraX face flow, signature
- full cross-platform rewrite is too expensive for current resource constraints
- token usage is lower if work is split into bounded modules

## Migration Principles

1. Build the app shell and core data layer first.
2. Migrate pure business pages before heavy platform pages.
3. Keep complex capability pages for later:
- map home
- trajectory playback
- realtime video
- video playback
- payment sdk
- face check
- signature
4. Maintain a clear parity checklist for each migrated page.
5. Prefer stable, boring iOS native architecture over clever abstractions.

## Recommended iOS Stack

Use:
- Swift
- UIKit first, unless a fully SwiftUI iOS codebase is already chosen
- URLSession or Alamofire
- Codable
- Kingfisher or SDWebImage
- SnapKit optional
- WKWebView where existing H5 bridge must be preserved

Why UIKit first:
- easier parity with current Android view-based project
- easier to control incremental migration and mixed complex screens

## Suggested Project Structure

Create a new iOS project separately from Android source.

Suggested folders:
- `ios-app/App`
- `ios-app/Core`
- `ios-app/Networking`
- `ios-app/Models`
- `ios-app/Modules/Login`
- `ios-app/Modules/Home`
- `ios-app/Modules/Report`
- `ios-app/Modules/Car`
- `ios-app/Modules/Training`
- `ios-app/Modules/Profile`
- `ios-app/Resources`
- `ios-app/Bridge`

Inside each module:
- `Views`
- `ViewControllers`
- `ViewModels`
- `Services`
- `Models`

## Phase Plan

### Phase 0: Setup

Target:
- runnable iOS shell
- base routing
- network layer
- login persistence
- environment config

Tasks:
- create iOS project
- add bundle id / build config / env config
- implement API client
- map common response model
- implement login screen
- implement tab bar shell
- implement user session storage
- define global style tokens

Done when:
- app launches
- login works
- tab navigation works
- authenticated requests work

### Phase 1: Low-Risk Business Pages

Priority modules:
- report
- notice
- pay record / order list
- car info
- training list pages

Tasks:
- migrate report home and report detail pages
- migrate notice list/detail
- migrate pay record list
- migrate vehicle info pages
- migrate training list, course list, simple detail pages

Done when:
- these pages can complete their main read/query flows
- UI matches current product reasonably
- no platform-specific native capability is blocking

### Phase 2: Mid-Risk Functional Pages

Priority modules:
- training detail
- certificate preview
- exam list / exam manager
- image upload pages

Tasks:
- migrate training detail flows
- migrate certificate and exam ticket rendering
- migrate photo upload related screens
- migrate agreement/notice read-confirm flows

Done when:
- core training flow is usable except face/sign/pay sdk dependent steps

### Phase 3: Heavy Native Capabilities

Priority modules:
- map home
- trajectory playback
- navigation
- realtime video
- video playback

Tasks:
- decide map vendor and iOS sdk path
- reimplement map marker clustering/status display
- reimplement trajectory playback
- evaluate whether video flow stays native + WKWebView bridge
- implement iOS video playback strategy

Done when:
- monitor core flow is usable on iOS

### Phase 4: Last Mile Native Features

Priority modules:
- WeChat pay
- Alipay
- face check
- signature

Tasks:
- implement payment sdk integration
- implement face capture / upload / validation flow
- implement signature canvas and upload
- finish app review and production hardening

Done when:
- payment and training闭环 complete

## Priority Order

Follow this order unless a blocking dependency changes it:

1. app shell
2. login
3. report
4. notice
5. pay record / order list
6. car info
7. training list and details
8. certificate / exam pages
9. map
10. video
11. payment sdk
12. face check
13. signature

## Page Mapping Guidance

When implementing a page:
1. find the Android Activity/Fragment
2. identify required API endpoints
3. identify models used by the page
4. identify special local state or timers
5. compare UI with `ytcar-app` if visual polish is needed
6. write the iOS page only after behavior is understood

Each migrated page should record:
- Android source file
- APIs used
- edge cases
- known deviations

## Important Android References

High-value files:
- `app/src/main/java/com/fx/zfcar/pages/LoginActivity.kt`
- `app/src/main/java/com/fx/zfcar/car/ReportActivity.kt`
- `app/src/main/java/com/fx/zfcar/training/notice/NoticeActivity.kt`
- `app/src/main/java/com/fx/zfcar/car/CarInfoActivity.kt`
- `app/src/main/java/com/fx/zfcar/car/CarInfoFragment.kt`
- `app/src/main/java/com/fx/zfcar/training/safetytraining/TrainListActivity.kt`
- `app/src/main/java/com/fx/zfcar/training/pay/PayOrderActivity.kt`
- `app/src/main/java/com/fx/zfcar/training/pay/PayDetailActivity.kt`
- `app/src/main/java/com/fx/zfcar/car/CarFragment.kt`
- `app/src/main/java/com/fx/zfcar/car/TrackPlayActivity.kt`
- `app/src/main/java/com/fx/zfcar/car/RealTimeMonitorActivity.kt`
- `app/src/main/java/com/fx/zfcar/car/VideoPlaybackActivity.kt`
- `app/src/main/java/com/fx/zfcar/training/safetytraining/FaceCheckActivity.kt`
- `app/src/main/java/com/fx/zfcar/training/notice/SignatureActivity.kt`

## Work Rules For Future Codex Sessions

Future Codex should:
- read this file first
- avoid proposing Flutter for the current migration track
- keep output focused on implementation, not architecture theory
- prefer small, shippable milestones
- avoid touching Android unless explicitly requested
- keep parity notes short and concrete
- compile and verify after each meaningful change

If blocked:
- document the blocker in this file or a nearby note
- propose the smallest unblocking path

## Definition Of Success

Success is not “all pages rewritten”.

Success means:
- iOS project exists and can run
- login works
- major read/query business flows work
- core training and report pages are usable
- complex native pages are migrated in controlled later phases
- project can continue across account switches without losing direction

## First Tasks After Account Switch

The next Codex session should start with:

1. create the iOS project skeleton
2. define iOS folder structure
3. implement networking base layer
4. inspect Android login flow and reproduce it on iOS
5. commit after shell + login base are running

## Notes

This plan intentionally optimizes for:
- lower token consumption
- lower cross-session context loss
- smaller implementation batches
- faster time to first usable iOS build
