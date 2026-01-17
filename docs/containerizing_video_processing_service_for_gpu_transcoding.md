# Containerizing Video Processing Service
There are some issues when containerizing `video-processing-service` for GPU transcoding. FFmpeg and docker _"know"_ that CUDA is available and have also located the GPU correctly, but for some reason are not able to invoke CUDA, which leads to the job failing.

However, GPU transcoding works fine when running outside of docker. Whilst running the service **inside docker**, it is recommended to use `ENCODING_MODE=cpu`. Instead of `ENCODING_MODE=gpu` or `ENCODING_MODE=auto`.

## General steps needed to be done on a host machine (Ubuntu)
1. Add Nvidia GPG Key
```bash
curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey \
| sudo gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg
```

2. Add Nvidia container repo
```bash
curl -fsSL https://nvidia.github.io/libnvidia-container/stable/deb/nvidia-container-toolkit.list \
| sed 's#deb https://#deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://#g' \
| sudo tee /etc/apt/sources.list.d/nvidia-container-toolkit.list
```

3. Update apt
```bash
sudo apt update
```

4. Install toolkit
```bash
sudo apt install -y nvidia-container-toolkit
```

5. Configure docker runtime
```bash
sudo nvidia-ctk runtime configure --runtime=docker
sudo systemctl restart docker
```