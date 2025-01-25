#!/bin/bash
# Arguments $1:서버유형(MAIN/TEST)

# MAIN 배포
if [ $1 = "MAIN"]; then
  # 이미지 내려받기
  echo "1. get image"
  sudo docker-compose pull gmk0904/class-connect-v2-backend-main:latest

  IS_GREEN=$(sudo docker ps | grep main-green) # 현재 실행중인 App이 blue인지 확인

  if [ -z $IS_GREEN  ];then # blue라면
    echo "### BLUE => GREEN ###"

    echo "2. main-green container up"
    sudo docker-compose up -d main-green # green 컨테이너 실행

    while [ 1 = 1 ]; do
      echo "3. green health check..."
      sleep 3

      REQUEST=$(curl http://127.0.0.1:8080) # green으로 request
       if [ -n "$REQUEST" ]; then # 서비스 가능하면 health check 중지
          echo "health check success"
          break ;
       fi
    done;

    echo "4. reload nginx"
    #cd ~/nginx-certbot/data/nginx
    #cp ./app.main-green.conf ./app.conf  # 설정파일 교체
    #cd ~/nginx-certbot
    sudo docker compose restart nginx

    echo "5. blue container down"
    cd ~/myapp
    sudo docker-compose stop main-blue
  else
    echo "### GREEN => BLUE ###"

    echo "2. blue container up"
    sudo docker-compose up -d main-blue

    while [ 1 = 1 ]; do
      echo "3. blue health check..."
      sleep 3
      REQUEST=$(curl http://127.0.0.1:8081) # blue로 request

      if [ -n "$REQUEST" ]; then # 서비스 가능하면 health check 중지
        echo "health check success"
        break ;
      fi
    done;

    echo "4. reload nginx"
    #cd ~/nginx-certbot/data/nginx
    #cp ./app.blue.conf ./app.conf  # 설정파일 교체
    #cd ~/nginx-certbot
    sudo docker compose restart nginx

    echo "5. green container down"
    cd ~/myapp
    sudo docker-compose stop main-green
  fi
# TEST 배포
elif [ $1 = "TEST" ]; then
  # 이미지 내려받기
  echo "1. get image"
  sudo docker-compose pull gmk0904/class-connect-v2-backend-test:latest

  IS_GREEN=$(sudo docker ps | grep test-green) # 현재 실행중인 App이 blue인지 확인

  if [ -z $IS_GREEN  ];then # blue라면
    echo "### BLUE => GREEN ###"

    echo "2. test-green container up"
    sudo docker-compose up -d test-green # green 컨테이너 실행

    while [ 1 = 1 ]; do
      echo "3. green health check..."
      sleep 3

      REQUEST=$(curl http://127.0.0.1:8080) # green으로 request
       if [ -n "$REQUEST" ]; then # 서비스 가능하면 health check 중지
          echo "health check success"
          break ;
       fi
    done;

    echo "4. reload nginx"
    #cd ~/nginx-certbot/data/nginx
    #cp ./app.test-green.conf ./app.conf  # 설정파일 교체
    #cd ~/nginx-certbot
    sudo docker compose restart nginx

    echo "5. blue container down"
    cd ~/myapp
    sudo docker-compose stop test-blue
  else
    echo "### GREEN => BLUE ###"

    echo "2. blue container up"
    sudo docker-compose up -d test-blue

    while [ 1 = 1 ]; do
      echo "3. blue health check..."
      sleep 3
      REQUEST=$(curl http://127.0.0.1:8081) # blue로 request

      if [ -n "$REQUEST" ]; then # 서비스 가능하면 health check 중지
        echo "health check success"
        break ;
      fi
    done;

    echo "4. reload nginx"
    #cd ~/nginx-certbot/data/nginx
    #cp ./app.blue.conf ./app.conf  # 설정파일 교체
    #cd ~/nginx-certbot
    sudo docker compose restart nginx

    echo "5. green container down"
    cd ~/myapp
    sudo docker-compose stop test-green
  fi
fi