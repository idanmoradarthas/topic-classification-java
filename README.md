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
      "text":"I was wondering if anyone out there could enlighten me on this car I saw\nthe other day. It was a 2-door sports car, looked to be from the late 60s/\nearly 70s. It was called a Bricklin. The doors were really small. In addition,\nthe front bumper was separate from the rest of the body. This is \nall I know. If anyone can tellme a model name, engine specs, years\nof production, where this car is made, history, or whatever info you\nhave on this funky looking car, please e-mail."
   },
   {
      "text":"this is a very long sentence. bla bla bla."
   },
   {
      "text":"guns guns guns"
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
        "weapons:": {
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
        "nudity": {
            "match_prob": 0.15136597226507809
        },
        "weapons": {
            "match_prob": 0.03909963272858064
        }
    },
    {
        "nudity": {
            "match_prob": 0.1077333421533001
        },
        "weapons": {
            "match_prob": 0.2061169369840805
        }
    },
    {
        "nudity": {
            "match_prob": 0.054650962737729784
        },
        "weapons": {
            "match_prob": 0.9975545874439852
        }
    }
]
```