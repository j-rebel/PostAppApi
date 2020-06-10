# PostApp Server

PostApp - https://github.com/j-rebel/PostApp

Heroku link - https://post-app-back.herokuapp.com/

## Routes

### Users

#### 1. POST /v1/users/create

params:
- displayName
- email
- password
- avatar

#### 2. POST /v1/users/login

params:
- email
- password

#### 3. /v1/users/logout - in progress
#### 4. /v1/users/delete - in progress

### Posts

#### 1. POST /v1/posts 

params:
- id
- posted_by
- date
- type
- repost
- text
- video
- address
- geo_long
- geo_lat

#### 2. GET /v1/posts 
#### 3. DELETE /v1/posts

params:
- id

#### 4. POST /v1/posts/like

params:
- user
- post

## Config Vars

JDBC_DRIVER = org.postgresql.Driver

JDBC_DATABASE_URL - set up path to a db

SECRET_KEY - used for hashing

JWT_SECRET - used for hashing



