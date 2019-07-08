# topic-classification
Classifying if text is related to weapons or not.

## Install and run project
build using ```Java 1.8``` with ```Spring-Boot 2.1.6.RELEASE```, ```Docker Compose 3``` and ```ElasticSearch 7.1.1```.

```
git clone https://github.com/idanmoradarthas/topic-classification-java.git
cd topic-classification-java
docker-compose up
```

## Interface v1.0
### Request and Response
Request Should be constructed as such and send in method GET:
```json
[
  {
  "text": "text to analyze"
  },
  ...
]
```
where each document will be in separate brackets.

The API is exposed in route "/classify/v1.0". so the full address should be ```localhost:8080/classify/v1.0```

for example:
```json
[  
   {  
      "text":"long time no see, wanna go to the range and shoot some? got new guns and lots of bullets."
   },
   {  
      "text":"guns guns guns"
   },
   {  
      "text":""
   },
   {  
      "text":"[]"
   },
   {  
      "text":"\n\t\t\t\t\t\n\t\t\n\t\tNew sign-in from Chrome on Windows\t\n\t\n\t\t\t\t\n\t\tHi TCFB,\n\tYour Google Account tcfb.mobile@gmail.com was just used to sign in from Chrome on Windows.\t\t\t\tTCFB Karaoke\ntcfb.mobile@gmail.com\n\t\t\n\t\t\t\tWindows\nWednesday, March 8, 2017 10:56 AM (GMT+8)\nSingapore*\nChrome\nDon\'t recognize this activity?\nReview your recently used devices now.\nWhy are we sending this? We take security very seriously and we want to keep you in the loop on important actions in your accoun .\nWe were unable to determine whether you have used this browser or device with your account before. This can happen when you sign in for the first time on a new computer, phone or browser, when you use your browser\'s incognito or private browsing mode or clear your cookies, or when somebody else is accessing your account.\n\tBest,\nThe Google Accounts team\n\t\t*The location is approximate and determined by the IP address it was coming from.\n\tThis email can\'t receive replies. To give us feedback on this alert, click here.\nFor more information, visit the Google Accounts Help Center.\n\t\n\t\tYou received this mandatory email service announcement to update you about important changes to your Google product or account.\n\t\xc2\xa9 2017 Google Inc., 1600 Amphitheatre Parkway, Mountain View, CA 94043, USA\n\t\n"
   },
   {  
      "text":"\n\t\t\t\t\n\t\t\t\t\t\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\xc2\xa0\n\t\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\xc2\xa0\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\xc2\xa0\n\t\xc2\xa0\n\t\n\t\t\t\t\t\t\t\t\t\t\t\n\t\n\t\t\t\n\t\n\t\t\t\t\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\tSign in\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\n\t\t\n\t\t\n\t\xc2\xa0\n\t\n\t\t\t\n\t\n\t\t\t\t\n\t\t\t\tNew matches near you\n\t\t\t\n\t\t\n\t\xc2\xa0\n\t\n\t\t\t\n\t\xc2\xa0\n\t\n\t\t\n\t\xc2\xa0\n\t\n\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\t\n\t\t\t\t\tlemonjars\n\t\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\n\t\t\t\t21\n\t\t\t\t\xc2\xb7\n\t\t\t\tSingapore, Singapore\n\t\t\t\n\t\t\t\n\t\t\t\n\t\n\t\t\t\t\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\tCheck them out\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\n\t\t\n\t\t\n\t\xc2\xa0\n\t\xc2\xa0\n\t\xc2\xa0\n\t\n\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\t\n\t\t\t\t\thellothredude\n\t\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\n\t\t\t\t18\n\t\t\t\t\xc2\xb7\n\t\t\t\tSingapore, Singapore\n\t\t\t\n\t\t\t\n\t\t\t\n\t\n\t\t\t\t\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\tCheck them out\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\n\t\t\n\t\t\n\t\xc2\xa0\n\t\xc2\xa0\n\t\xc2\xa0\n\t\n\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\t\n\t\t\t\t\tgravitydefying9\n\t\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\n\t\t\t\t22\n\t\t\t\t\xc2\xb7\n\t\t\t\tSingapore, Singapore\n\t\t\t\n\t\t\t\n\t\t\t\n\t\n\t\t\t\t\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\tCheck them out\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\n\t\t\n\t\t\n\t\xc2\xa0\n\t\xc2\xa0\n\t\xc2\xa0\n\t\n\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\t\n\t\t\t\t\tSunshineShaa\n\t\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\n\t\t\t\t22\n\t\t\t\t\xc2\xb7\n\t\t\t\tSingapore, Singapore\n\t\t\t\n\t\t\t\n\t\t\t\n\t\n\t\t\t\t\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\tCheck them out\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\n\t\t\n\t\t\n\t\xc2\xa0\n\t\xc2\xa0\n\t\xc2\xa0\n\t\n\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\t\n\t\t\t\t\tairah_zirah\n\t\t\t\t\n\t\t\t\t\n\t\t\t\n\t\t\t\n\t\t\t\t21\n\t\t\t\t\xc2\xb7\n\t\t\t\tSingapore, Singapore\n\t\t\t\n\t\t\t\n\t\t\t\n\t\n\t\t\t\t\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\tCheck them out\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\n\t\t\n\t\t\n\t\xc2\xa0\n\t\xc2\xa0\n\t\n\t\t\n\t\xc2\xa0\n\t\n\t\t\t\n\t\t\t\t\t\n\t\t\t\t\t\tBrowse More Matches \xe2\x86\x92\n\t\t\t\t\t\n\t\t\t\t\n\t\t\n\t\xc2\xa0\n\t\n\t\t\t\n\t\n\t\t\t\t\n\t\t\t\tVisited You\n\t\t\t\n\t\t\n\t\xc2\xa0\n\t\n\t\t\t\n\t\t\t\t\t\n\t\t\t\t\t\t\n\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\n\t\t\t\n\t\t\n\t\xc2\xa0\n\t\n\t\t\t\n\t\t\t\t\t\n\t\t\t\t\t\tView All Visitors \xe2\x86\x92\n\t\t\t\t\t\n\t\t\t\t\n\t\t\n\t\xc2\xa0\n\t\n\t\t\t\n\t\n\t\t\t\t\n\t\t\t\tImprove your matches\n\t\t\t\n\t\t\n\t\xc2\xa0\n\t\n\t\t\t\n\t\n\t\t\t\t\n\t\t\t\tAre you ready to settle down and get married right now?\n\t\t\t\n\t\xc2\xa0\n\t\n\t\t\t\t\n\t\t\t\tAbsolutely\n\t\t\t\n\t\xc2\xa0\n\t\n\t\t\t\t\n\t\t\t\tNo way\n\t\t\t\n\t\xc2\xa0\n\t\n\t\t\t\t\n\t\t\t\tGet married yes, settle down no\n\t\t\t\n\t\xc2\xa0\n\t\n\t\t\t\t\n\t\t\t\tGet married no, settle down yes\n\t\t\t\n\t\xc2\xa0\n\t\t\n\t\xc2\xa0\n\t\n\t\t\t\n\t\t\t\t\t\n\t\t\t\t\t\tAnswer More Questions \xe2\x86\x92\n\t\t\t\t\t\n\t\t\t\t\n\t\t\n\t\xc2\xa0\n\t\n\t\t\t\n\t\n\t\t\t\t\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\tSign in\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\n\t\t\n\t\t\n\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\t\t\n\t\xc2\xa0\n\t\t\t\t\t\t\t\t\n\t\n\t\t\t\t\n\t\t\t\t\t\t\n\t\xc2\xa0\n\t\n\t\n\t\t\n\t\n\t\n\t\t\n\t\n\t\xc2\xa0\n\t\n\t\n\t\tOkCupid \xc2\xa9 2017 Humor Rainbow, Inc.\n\t\t\n\t\t\n\t\t\n\t\t\t555 W 18th St, New York, NY 10011\n\t\t\n\t\n\t\n\t\t\n\t\t\tUnsubscribe\n\t\t\n\t\n\t\xc2\xa0\n\t\t\t\t\t\n\t\n\t\t\t\t\t\tADVERTISEMENT\n\t\t\t\t\t\t\n\t\n\t\t\t\t\t\n\t\t\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\n\t\t\t\t\t\n\t\t\t\t\n\t\t\t\t\n\t\t\t\t\n\t\t\t"
   }
]
```

Response will an array in same size as the documents with matching probability for weapon mentioning in the doc:
```json
[
  {
        "nudity": {
          "match_prob": prob
        },
        "drugs": {
          "match_prob": prob
        },
        "cyber-security": {
          "match_prob": prob
        },
        "weapons": {
          "match_prob": prob
        }
  },
  ...
]
```
for example:
```json
[
   {
      "nudity":{
         "match_prob":0.0
      },
      "drugs":{
         "match_prob":0.0
      },
      "cyber-security":{
         "match_prob":0.0
      },
      "weapons":{
         "match_prob":0.3076923076923077
      }
   },
   {
      "nudity":{
         "match_prob":0.0
      },
      "drugs":{
         "match_prob":0.0
      },
      "cyber-security":{
         "match_prob":0.0
      },
      "weapons":{
         "match_prob":0.97
      }
   },
   {
      "nudity":{
         "match_prob":0.0
      },
      "drugs":{
         "match_prob":0.0
      },
      "cyber-security":{
         "match_prob":0.0
      },
      "weapons":{
         "match_prob":0.0
      }
   },
   {
      "nudity":{
         "match_prob":0.0
      },
      "drugs":{
         "match_prob":0.0
      },
      "cyber-security":{
         "match_prob":0.0
      },
      "weapons":{
         "match_prob":0.0
      }
   },
   {
      "nudity":{
         "match_prob":0.0
      },
      "drugs":{
         "match_prob":0.0
      },
      "cyber-security":{
         "match_prob":0.0
      },
      "weapons":{
         "match_prob":0.0
      }
   },
   {
      "nudity":{
         "match_prob":0.08565079067757202
      },
      "drugs":{
         "match_prob":0.0
      },
      "cyber-security":{
         "match_prob":0.0
      },
      "weapons":{
         "match_prob":0.0
      }
   }
]
```