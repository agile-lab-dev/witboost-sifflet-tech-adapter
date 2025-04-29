# Data Quality endpoint

This endpoint is designed to support integration with a micro-frontend that shows data quality reports related to an output port. Currently, only AWS Athena output ports are supported and their full descriptors are expected as input to the request.

**Request method**: `POST`
**Endpoint**: `/v1/dataquality`

## Example Request Body
The request must include the full descriptor of the output port. Example:

```json

{
    "outputport": {
        "id": "urn:dmb:cmp:finance:demo:0:references",
        "info": {
            "view": {
                "type": "string",
                "label": "View name",
                "value": "references"
            },
            "catalog": {
                "type": "string",
                "label": "Catalog",
                "value": "AWSDataCatalog"
            },
            "database": {
                "type": "string",
                "label": "Database",
                "value": "finance_development_demo_v0_consumable"
            }
        },
        "kind": "outputport",
        "name": "references",
        "tags": [],
        "version": "0.0.0",
        "platform": "AWS",
        "specific": {
            "view": {
                "name": "references",
                "catalog": "AWSDataCatalog",
                "database": "finance_development_demo_v0_consumable"
            },
            "sourceTable": {
                "name": "john_test",
                "catalog": "AWSDataCatalog",
                "database": "finance_development_demo_v0_internal",
                "tableFormat": "ICEBERG"
            },
            "storageAreaId": "urn:dmb:cmp:finance:demo:0:s3-storage-area"
        },
        "dependsOn": [
            "urn:dmb:cmp:finance:demo:0:s3-storage-area"
        ],
        "shoppable": true,
        "startDate": "2025-03-25T15:28:52.169Z",
        "consumable": true,
        "sampleData": {},
        "technology": "Athena",
        "description": "Output Port that exposes information of this Data Product to users and other data products.",
        "creationDate": "2025-03-25T15:28:52.169Z",
        "dataContract": {
            "SLA": {
                "upTime": null,
                "timeliness": null,
                "intervalOfChange": null
            },
            "schema": [
                {
                    "name": "id",
                    "tags": [],
                    "dataType": "INT"
                },
                {
                    "name": "name",
                    "tags": [],
                    "dataType": "STRING"
                }
            ],
            "quality": [
                {
                    "type": "custom",
                    "engine": "sifflet",
                    "implementation": {
                        "name": "id not null",
                        "incident": {
                            "severity": "Low",
                            "createOnFailure": false
                        },
                        "schedule": "@daily",
                        "parameters": {
                            "kind": "FieldNulls",
                            "field": "id",
                            "threshold": {
                                "max": "0%",
                                "kind": "Static",
                                "valueMode": "Percentage"
                            },
                            "nullValues": "NullAndEmpty"
                        },
                        "description": null,
                        "scheduleTimezone": "UTC"
                    }
                },
                {
                    "type": "custom",
                    "engine": "sifflet",
                    "implementation": {
                        "name": "name unique",
                        "incident": {
                            "severity": "Low",
                            "createOnFailure": false
                        },
                        "schedule": "@daily",
                        "parameters": {
                            "kind": "FieldDuplicates",
                            "field": "name"
                        },
                        "description": null,
                        "scheduleTimezone": "UTC"
                    }
                }
            ],
            "endpoint": null,
            "termsAndConditions": null
        },
        "outputPortType": "View",
        "semanticLinking": [],
        "useCaseTemplateId": "urn:dmb:utm:aws-athena-template:0.0.0",
        "fullyQualifiedName": "Finance - demo - version 0 - references",
        "processDescription": null,
        "dataSharingAgreement": {
            "billing": null,
            "purpose": null,
            "security": null,
            "lifeCycle": null,
            "limitations": null,
            "intendedUsage": null,
            "confidentiality": null
        },
        "infrastructureTemplateId": "urn:dmb:itm:aws-athena-tech-adapter:0"
    }
}
```


## Example Response Body

An example of the body of the response is shown below:

```json
[
    {
        "id": "<monitorID>",
        "name": "Unique Monitor",
        "ruleLabel": "Unique",
        "criticality": "LOW",
        "lastRunId": "27ce5a24-4070-40f9-b721-a6504430570f",
        "lastRunTimestamp": 1742947739000,
        "lastRunStatus": "FAILED",
        "lastRunResult": "Anomaly detected in 1 timeslot: [2025-03-26] Value = 1 Expected = [value < 0.0].",
        "lastRunsMonitorRuns": 9,
        "lastRunsMonitorSuccess": 11.11111111111111,
        "lastRunsMonitorFailed": 22.22222222222222,
        "lastRunsMonitorAttentionRequired": 0.0,
        "lastRunsMonitorAttentionTechicalError": 66.66666666666667,
        "lastRunsStatuses": [
            {
                "timestamp": 1742947739000,
                "status": "FAILED",
                "result": "Anomaly detected in 1 timeslot: [2025-03-26] Value = 1 Expected = [value < 0.0]."
            },
            {
                "timestamp": 1742941941000,
                "status": "FAILED",
                "result": "Anomaly detected in 1 timeslot: [2025-03-25] Value = 1 Expected = [value < 0.0]."
            },
            {
                "timestamp": 1742941901000,
                "status": "SUCCESS",
                "result": "No anomaly was detected."
            },
            {
                "timestamp": 1742935234000,
                "status": "TECHNICAL_ERROR",
                "result": "Could not execute fetching data query on database: [s3://bucketName/v0]."
            },
            {
                "timestamp": 1742935197000,
                "status": "TECHNICAL_ERROR",
                "result": "Could not execute fetching data query on database: [s3://bucketName/v0/sifflet]."
            },
            {
                "timestamp": 1742935168000,
                "status": "TECHNICAL_ERROR",
                "result": "Could not execute fetching data query on database: [s3://bucketName/v0/sifflet/]."
            },
            {
                "timestamp": 1742935005000,
                "status": "TECHNICAL_ERROR",
                "result": "Could not execute fetching data query on database: [s3://bucketName/v0/athena/outputport/sifflet/]."
            },
            {
                "timestamp": 1742918404000,
                "status": "TECHNICAL_ERROR",
                "result": "Could not execute fetching data query on database: [s3://bucketName/v0/athena/outputport/sifflet/]."
            },
            {
                "timestamp": 1742918404000,
                "status": "TECHNICAL_ERROR",
                "result": "Could not execute fetching data query on database: [s3://bucketName/v0/athena/outputport/sifflet/]."
            }
        ]
    },
    {
        "id": "<monitorID>",
        "name": "Schema Change Monitor",
        "ruleLabel": "Schema Change",
        "criticality": "HIGH",
        "lastRunId": null,
        "lastRunTimestamp": null,
        "lastRunStatus": null,
        "lastRunResult": null,
        "lastRunsMonitorRuns": 0,
        "lastRunsMonitorSuccess": 0.0,
        "lastRunsMonitorFailed": 0.0,
        "lastRunsMonitorAttentionRequired": 0.0,
        "lastRunsMonitorAttentionTechicalError": 0.0,
        "lastRunsStatuses": []
    },
    {
        "id": "<monitorID>",
        "name": "Row-level Duplicates Monitor",
        "ruleLabel": "Row-level Duplicates",
        "criticality": "CRITICAL",
        "lastRunId": null,
        "lastRunTimestamp": null,
        "lastRunStatus": null,
        "lastRunResult": null,
        "lastRunsMonitorRuns": 0,
        "lastRunsMonitorSuccess": 0.0,
        "lastRunsMonitorFailed": 0.0,
        "lastRunsMonitorAttentionRequired": 0.0,
        "lastRunsMonitorAttentionTechicalError": 0.0,
        "lastRunsStatuses": []
    },
    {
        "id": "<monitorID>",
        "name": "Nulls Monitor on id",
        "ruleLabel": "Nulls",
        "criticality": "MODERATE",
        "lastRunId": "ec113eac-02b6-430c-a6e8-e77f5669b571",
        "lastRunTimestamp": 1742947283000,
        "lastRunStatus": "FAILED",
        "lastRunResult": "Anomaly detected in 1 timeslot: [2025-03-26] Value = 1 Expected = [value < 0.0].",
        "lastRunsMonitorRuns": 7,
        "lastRunsMonitorSuccess": 57.142857142857146,
        "lastRunsMonitorFailed": 28.571428571428573,
        "lastRunsMonitorAttentionRequired": 0.0,
        "lastRunsMonitorAttentionTechicalError": 14.285714285714286,
        "lastRunsStatuses": [
            {
                "timestamp": 1742947282000,
                "status": "FAILED",
                "result": "Anomaly detected in 1 timeslot: [2025-03-26] Value = 1 Expected = [value < 0.0]."
            },
            {
                "timestamp": 1742942161000,
                "status": "FAILED",
                "result": "Anomaly detected in 1 timeslot: [2025-03-25] Value = 1 Expected = [value < 0.0]."
            },
            {
                "timestamp": 1742941978000,
                "status": "SUCCESS",
                "result": "No anomaly was detected."
            },
            {
                "timestamp": 1742941977000,
                "status": "SUCCESS",
                "result": "No anomaly was detected."
            },
            {
                "timestamp": 1742941977000,
                "status": "SUCCESS",
                "result": "No anomaly was detected."
            },
            {
                "timestamp": 1742941977000,
                "status": "SUCCESS",
                "result": "No anomaly was detected."
            },
            {
                "timestamp": 1742918406000,
                "status": "TECHNICAL_ERROR",
                "result": "Could not execute fetching data query on database: [s3://bucketName/v0/athena/outputport/sifflet/]."
            }
        ]
    }
]
``
