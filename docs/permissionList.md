# AWS permissions


To integrate Sifflet with Amazon Athena, you need to define an **IAM Role** in the application configuration (`application.yml`) under the property **`sifflet.athena.iamRole`**. This role must include the permissions listed below.

### Key Information:
- The IAM role allows Sifflet to create and query Athena sources.
- The configuration for this role is documented in [this file](../README.md) under the `Configuring` section.
- Adjust the permissions below to follow the least privilege principle by narrowing down the `"*"` resources to your specific use case.

Below is an example of the required IAM policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "Athena",
      "Effect": "Allow",
      "Action": [
        "athena:StartQueryExecution",
        "athena:GetQueryExecution",
        "athena:GetQueryResults",
        "athena:GetQueryResultsStream",
        "athena:ListQueryExecutions",
        "athena:CreatePreparedStatement",
        "athena:DeletePreparedStatement",
        "athena:GetPreparedStatement",
        "athena:GetWorkGroup",
        "athena:ListDataCatalogs",
        "athena:GetDatabase",
        "athena:ListDatabases",
        "athena:ListTableMetadata",
        "athena:GetTableMetadata"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Sid": "Glue",
      "Effect": "Allow",
      "Action": [
        "glue:GetDatabase",
        "glue:GetDatabases",
        "glue:GetTable",
        "glue:GetTables",
        "glue:GetPartition",
        "glue:GetPartitions",
        "glue:BatchGetPartition"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Sid": "S3",
      "Effect": "Allow",
      "Action": [
        "s3:GetBucketLocation",
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Sid": "LakeFormation",
      "Effect": "Allow",
      "Action": [
        "lakeformation:GetDataAccess"
      ],
      "Resource": "*"
    }
  ]
}
```
