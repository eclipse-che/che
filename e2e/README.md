
# Module for launch E2E tests related to Che 7

## Requirements

- node 8.x
- "Chrome" browser 69.x or later
- deployed Che 7 with accessible URL

## Before launch

**Perform commands:**

- ```export TS_SELENIUM_BASE_URL=<Che7 URL>```
- ```npm install```

## Default launch

- ```npm test```

## Custom launch

- Use environment variables which described in the **```'TestConstants.ts'```** file
