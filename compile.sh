cd webserver
find -name "*.java" > ../javafiles.txt
javac -cp /home/ec2-user/lib/Jama-1.0.3.jar:/home/ec2-user/lib/aws-java-sdk-1.11.328/lib/aws-java-sdk-1.11.328.jar:/home/ec2-user/lib/aws-java-sdk-1.11.328/third-party/lib/*:.  @../javafiles.txt
rm ../javafiles.txt
