# 1. ビルド環境（MavenとJava 21が入った環境を使う）
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# テストをスキップしてビルド（時短とエラー回避のため）
RUN mvn clean package -DskipTests

# 2. 実行環境（Java 21だけが入った軽量な環境を使う）
FROM eclipse-temurin:21-jdk
WORKDIR /app
# ビルド環境で作ったJARファイルをここにコピー
COPY --from=build /app/target/*.jar app.jar

# ポート8080を開放
EXPOSE 8080

# アプリ起動コマンド
ENTRYPOINT ["java", "-jar", "app.jar"]