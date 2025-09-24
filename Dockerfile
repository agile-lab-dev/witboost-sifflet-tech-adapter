FROM maven:3.9-eclipse-temurin-17

# Install Python and pip
RUN apt-get update && apt-get install -y python3 python3-pip python3-venv && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Create a virtual environment and install sifflet CLI
RUN python3 -m venv /opt/venv && \
    /opt/venv/bin/pip install --upgrade pip && \
    /opt/venv/bin/pip install sifflet==0.3.22

# Ensure the virtual environment is activated in the PATH
ENV PATH="/opt/venv/bin:$PATH"

COPY common/target/*.jar .

RUN curl -o opentelemetry-javaagent.jar -L https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.29.0/opentelemetry-javaagent.jar

COPY run_app.sh .

RUN chmod +x run_app.sh

ENTRYPOINT ["bash", "run_app.sh"]
