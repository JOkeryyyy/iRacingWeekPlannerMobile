# Project Brief

## Product Goal

iRacing Week Planner Mobile helps iRacing drivers browse upcoming races, choose a week or date, filter race options, and plan around cars and tracks they own or care about.

The mobile app should provide the planner experience from the existing web project in a native-feeling Android and iOS application.

## Source Repositories

- Existing web/source-of-truth repo: `/Users/gaojiahao/Documents/iracing/iRacing-week-planner`
- Mobile repo: `/Users/gaojiahao/Documents/GitHub/iRacingWeekPlannerMobile`

The web repo and scraper remain responsible for producing schedule data. The mobile app consumes generated JSON and does not scrape iRacing directly.

## MVP Scope

The MVP is planner-only:

- Race list
- Date/week selector
- Filters
- Sorting
- Owned car and owned track settings
- Favorite car and favorite track settings
- Cached schedule data
- Offline use after first successful data load

## Non-Goals for v1

- Firebase login
- Cross-device settings sync
- iRacing credentials inside the mobile app
- iRacing scraping inside the mobile app
- Full account management
- Advanced analytics or recommendations
- Web app rewrite

## Release Target

The first release target is an internal beta for validating the planner workflow, data contract, offline behavior, and mobile UX.

## Development Cadence

- Solo developer
- 2-week sprints
- Story-sized implementation
- Verification required before calling a story done
