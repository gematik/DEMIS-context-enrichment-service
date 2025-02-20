<img align="right" width="250" height="47" src="../media/Gematik_Logo_Flag.png"/> <br/>


# Context-Enrichment-Service (CES)

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
       <ul>
        <li><a href="#quality-gate">Quality Gate</a></li>
        <li><a href="#release-notes">Release Notes</a></li>
      </ul>
	</li>
    <li>
      <a href="#getting-started">Getting Started</a>
    </li>
    <li>
      <a href="#usage">Usage</a>
      <ul>
        <li><a href="#endpoints">Endpoints</a></li>
      </ul>
    </li>
    <li><a href="#security-policy">Security Policy</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

## About The Project

In DEMIS users with various different backgrounds such as health care professionals, laboratory staff as well as citizens 
(in behalf of their company) can submit notifications. They use different authentication methods such as certificates, 
SMC-Bs as well as third party identity providers like BundID which provide varying levels of trust concerning the 
identification of the notifying person.

The Context Enrichment Service (CES) extracts information concerning the authentication and associated level of trust 
from the user's JWT and provides a FHIR resource to embed it into the notification by the calling services. That way 
RKI and health offices can better assess the credibility of the notifications received.

### Quality Gate
[![Quality Gate Status](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Acontext-enrichment-service&metric=alert_status&token=sqb_b12c386a101bc2aecab21ec6ceffd41f9d8598b8)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Acontext-enrichment-service)
[![Vulnerabilities](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Acontext-enrichment-service&metric=vulnerabilities&token=sqb_b12c386a101bc2aecab21ec6ceffd41f9d8598b8)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Acontext-enrichment-service)
[![Bugs](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Acontext-enrichment-service&metric=bugs&token=sqb_b12c386a101bc2aecab21ec6ceffd41f9d8598b8)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Acontext-enrichment-service)
[![Code Smells](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Acontext-enrichment-service&metric=code_smells&token=sqb_b12c386a101bc2aecab21ec6ceffd41f9d8598b8)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Acontext-enrichment-service)
[![Lines of Code](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Acontext-enrichment-service&metric=ncloc&token=sqb_b12c386a101bc2aecab21ec6ceffd41f9d8598b8)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Acontext-enrichment-service)
[![Coverage](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Acontext-enrichment-service&metric=coverage&token=sqb_b12c386a101bc2aecab21ec6ceffd41f9d8598b8)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Acontext-enrichment-service)

[![Quality gate](https://sonar.prod.ccs.gematik.solutions/api/project_badges/quality_gate?project=de.gematik.demis%3Acontext-enrichment-service&token=sqb_b12c386a101bc2aecab21ec6ceffd41f9d8598b8)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Acontext-enrichment-service)

### Release Notes
See [ReleaseNotes](../ReleaseNotes.md) for all information regarding the (newest) releases.

## Getting Started
The application can be executed from a mvn command file or a Docker Image.
```sh
mvn clean verify
```

The docker image can be built with the following command:
```docker
docker build -t context-enrichment-service:latest .
```

The image can alternatively also be built with maven:
```sh
mvn -e clean install -Pdocker
```

## Usage
The application can be started as Docker container with the following commands:
```shell
docker run --rm --name context-enrichment-service -p 8080:8080 context-enrichment-service:latest
```

### Endpoints
Subsequently we list the endpoints provided by the Context-Enrichment-Service.

| HTTP Method | Endpoint    | Parameters                                                   | Body                                    | Returns                     |
|-------------|-------------|--------------------------------------------------------------|-----------------------------------------|-----------------------------|
| POST        | /enrichment | - header: Authorization (JWT TOKEN)<br>- body: CompositionId | The UUID of the Composition to refer to.| - FHIR-Provenance-Ressource |



## Security Policy
If you want to see the security policy, please check our [SECURITY.md](SECURITY.md).

## Contributing
If you want to contribute, please check our [CONTRIBUTING.md](CONTRIBUTING.md).

## License
EUROPEAN UNION PUBLIC LICENCE v. 1.2

EUPL © the European Union 2007, 2016

Copyright (c) 2023 gematik GmbH

See [LICENSE](../LICENSE.md).

## Contact
E-Mail to [DEMIS Entwicklung](mailto:demis-entwicklung@gematik.de?subject=[GitHub]%20Context-Enrichment-Service)
