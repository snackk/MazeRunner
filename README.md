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
http://localhost:8080/r.html?x_start=3&y_start=9&x_final=78&y_final=89&velocity=50&strategy=astar&maze_file_input=Maze100&maze_file_output_html=Maze100
  
  Written by Diogo Santos.
