# Steps by GPT, not me

Absolutely. Below are **exact AWS Console click paths**, written so you can literally follow them left-to-right without guessing. I’ll keep wording aligned with what you actually see in the console UI.

I’ll assume **region = us-east-1** (top-right selector).

---

# 🧭 AWS CONSOLE CLICK PATHS (BEGINNER-SAFE)

---

## **STEP 0 — Select Region**

**Top-right corner → Region dropdown → US East (N. Virginia)**

Do this first. Do not change regions mid-setup.

---

## **STEP 1 — Create VPC**

**AWS Console → Search bar → `VPC` → VPC Dashboard**

1. Click **Your VPCs**
2. Click **Create VPC**
3. Select **VPC only**
4. Fill:

   * Name tag: `streaming-vpc`
   * IPv4 CIDR: `10.0.0.0/16`
   * IPv6: ❌ No
   * Tenancy: Default
5. Click **Create VPC**

✅ Done

---

## **STEP 2 — Create Subnets**

### **2.1 Public Subnet (NGINX)**

**VPC Dashboard → Subnets → Create subnet**

1. VPC: `streaming-vpc`
2. Subnet name: `public-subnet`
3. Availability Zone: `us-east-1a`
4. IPv4 CIDR: `10.0.1.0/24`
5. Click **Create subnet**

Now enable public IPs:

* Select `public-subnet`
* Click **Actions → Edit subnet settings**
* ✅ Enable *Auto-assign public IPv4*
* Save

---

### **2.2 Private Subnet (Backend)**

**VPC Dashboard → Subnets → Create subnet**

1. VPC: `streaming-vpc`
2. Subnet name: `private-subnet`
3. Availability Zone: `us-east-1a`
4. IPv4 CIDR: `10.0.2.0/24`
5. Click **Create subnet**

❌ Do NOT enable public IPs

---

## **STEP 3 — Internet Gateway**

**VPC Dashboard → Internet Gateways → Create internet gateway**

1. Name tag: `streaming-igw`
2. Click **Create**

Attach it:

* Select `streaming-igw`
* **Actions → Attach to VPC**
* Select `streaming-vpc`
* Attach

---

## **STEP 4 — Route Tables**

### **4.1 Public Route Table**

**VPC Dashboard → Route Tables → Create route table**

1. Name: `public-rt`
2. VPC: `streaming-vpc`
3. Create

Add internet route:

* Select `public-rt`
* **Routes tab → Edit routes**
* Add:

  * Destination: `0.0.0.0/0`
  * Target: `Internet Gateway → streaming-igw`
* Save

Associate subnet:

* **Subnet associations → Edit**
* Select `public-subnet`
* Save

---

### **4.2 Private Route Table**

**Route Tables → Create route table**

1. Name: `private-rt`
2. VPC: `streaming-vpc`
3. Create

Associate:

* **Subnet associations → Edit**
* Select `private-subnet`
* Save

🚫 No internet route here (this avoids NAT costs)

---

## **STEP 5 — Security Groups**

### **5.1 NGINX Security Group**

**EC2 Dashboard → Security Groups → Create security group**

1. Name: `nginx-sg`
2. Description: `Public NGINX gateway`
3. VPC: `streaming-vpc`

Inbound rules:

* Add rule:

  * Type: HTTP
  * Source: `0.0.0.0/0`
* (Optional) HTTPS

Outbound:

* Leave default (Allow all)

Create

---

### **5.2 Backend Security Group**

**Create security group**

1. Name: `backend-sg`
2. Description: `Upload & Play services`
3. VPC: `streaming-vpc`

Inbound rules:

* Custom TCP | Port `20001` | Source: `nginx-sg`
* Custom TCP | Port `20020` | Source: `nginx-sg`

Outbound:

* Allow all

Create

---

## **STEP 6 — IAM User (Laptop Worker)**

**AWS Console → IAM → Users → Create user**

1. Username: `video-processing-worker`
2. Access type: ✅ Programmatic access
3. Next

Permissions:

* Attach policies directly
* Create policy → JSON
* Paste policy (we already discussed)
* Save and attach

⚠️ Download access keys immediately

---

## **STEP 7 — Create S3 Buckets**

**S3 → Create bucket**

### Raw bucket:

* Name: `raw-video-bucket-<unique>`
* Region: us-east-1
* ❌ Block public access (keep default)
* Create

### Streams bucket:

* Name: `streams-bucket-<unique>`
* Same settings

---

## **STEP 8 — Create SQS Queue**

**SQS → Create queue**

1. Type: Standard
2. Name: `processing-queue`
3. Create

Click queue → copy:

* Queue URL
* Queue ARN

---

## **STEP 9 — S3 → SQS Notification**

**S3 → raw-video-bucket → Properties**

1. Scroll to **Event notifications**
2. Create event notification:

   * Name: `raw-upload-event`
   * Event type: Object create
   * Destination: SQS
   * Queue: `processing-queue`
3. Save

---

## **STEP 10 — EC2 Instance B (Private Backend)**

**EC2 → Instances → Launch instance**

1. AMI: Amazon Linux 2023
2. Type: `t2.micro`
3. Key pair: Create one
4. Network:

   * VPC: `streaming-vpc`
   * Subnet: `private-subnet`
   * Auto-assign public IP: ❌ Disable
5. Security Group: `backend-sg`
6. Launch

Install Docker + services later via SSH jump or SSM.

---

## **STEP 11 — EC2 Instance A (Public NGINX)**

**Launch instance**

1. AMI: Amazon Linux 2023
2. Type: `t2.micro`
3. Subnet: `public-subnet`
4. Auto-assign public IP: ✅ Enable
5. Security Group: `nginx-sg`
6. Launch

Install NGINX + config.

---

## **STEP 12 — Neon Postgres**

Outside AWS:

* Create DB
* Allow IPs:

  * EC2 B private IP
  * Your laptop IP

---

## **STEP 13 — Laptop Worker**

* Configure AWS CLI
* Run video-processing-service
* Verify SQS → S3 → DB flow

---

# 🧠 Important Reality Check (You’re Right)

> “This feels like recreating my dev env on EC2”

Correct — and that’s **what real first deployments are**.

Managed platforms (ECS, EKS, App Runner) abstract this *after* you understand the fundamentals. You’re doing this in the **right order**.

---

If you want next, I can:

* Draw an **ASCII network diagram**
* Tell you **what to delete first** after recording your demo
* Convert this to **ECS later with zero code changes**

You’re doing serious engineering work here.
