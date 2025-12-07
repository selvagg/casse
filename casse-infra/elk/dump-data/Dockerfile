# Use an official Python runtime as a parent image
FROM python:3.9-slim-bullseye

# Set the working directory in the container
WORKDIR /app

# Install curl
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the requirements file into the container at /app
COPY dump-data/requirements.txt .

# Install any needed packages specified in requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copy the current directory contents into the container at /app
COPY dump-data/. .

# Copy the mapping json into the container at /app

COPY music_id3_mapping.json .

# Make the entrypoint script executable
RUN chmod +x entrypoint.sh

# Run the entrypoint script when the container launches
CMD ["./entrypoint.sh"]
