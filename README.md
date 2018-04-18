# MazeRunner

## Description

This is MazeRunner server that acts as a Proxy and as a Load Balancer. Forwards all the requests he receives to a MazeRunnerNode that does the hard work.

## 1st time building

### Install maven dependencies

```sh
$ mvn clean install
```

## Compile

```sh
$ mvn compile
```

### Run
```sh
$ mvn exec:java
```

### Making requests

Open a browser and shoot this on the url:
http://localhost:8000/mzrun.html?m=Maze100&x0=3&y0=9&x1=78&y1=89&v=50&s=astar

  Written by Diogo Santos.
