<p align="center">
  <img src="https://raw.githubusercontent.com/flamingock/flamingock-java/master/misc/logo-with-text.png" width="420px" alt="Flamingock logo" />
</p>

<h3 align="center">Template API</h3>
<p align="center">The public Java API for building Flamingock change templates.</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.flamingock/flamingock-template-api">
    <img src="https://img.shields.io/maven-central/v/io.flamingock/flamingock-template-api" alt="Maven Version" />
  </a>
  <a href="https://github.com/flamingock/flamingock-java-template-api/actions/workflows/build.yml">
    <img src="https://github.com/flamingock/flamingock-java-template-api/actions/workflows/build.yml/badge.svg" alt="Build" />
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License" />
  </a>
</p>

<p align="center">
  <a href="https://docs.flamingock.io/templates/create-your-own-template"><strong>Full Documentation</strong></a>
</p>

---

## 🧩 What is this?

This library provides the **annotations, interfaces, and base classes** that template authors use to build custom [Flamingock](https://github.com/flamingock/flamingock-java) change templates. Templates allow end users to define changes declaratively in YAML — without writing Java code.

This is the **API only**. Actual template implementations live in their own repositories:

- Looking to **use** an existing template? See [SQL Template](https://github.com/flamingock/flamingock-java-template-sql) or [MongoDB Template](https://github.com/flamingock/flamingock-java-template-mongodb).
- Looking to **create** your own template? You're in the right place.

---

## 🔑 Key Components

| Component | Type | Description |
|-----------|------|-------------|
| `@ChangeTemplate` | Annotation | Marks a class as a template; configures `name`, `multiStep`, and `rollbackPayloadRequired` |
| `@ApplyTemplate` | Annotation | Marks the method containing forward change logic |
| `@RollbackTemplate` | Annotation | Marks the method containing rollback logic |
| `AbstractChangeTemplate<C, A, R>` | Base class | Resolves generic types at runtime, provides GraalVM reflection metadata, and manages payload lifecycle |
| `TemplatePayload` | Interface | Contract for apply/rollback payloads — extends `TemplateField` and adds transaction support metadata via `getInfo()` |
| `TemplateField` | Interface | Base contract for all field types; provides `validate()` for load-time validation |
| `TemplateVoid` | Wrapper | Sentinel for "no value needed" — use as a generic type parameter when a slot is unused |
| `TemplateString` | Wrapper | Wraps a raw `String` payload for YAML scalar support |

---

## 📦 Installation

### Gradle

```kotlin
implementation("io.flamingock:flamingock-template-api:[VERSION]")
```

### Maven

```xml
<dependency>
    <groupId>io.flamingock</groupId>
    <artifactId>flamingock-template-api</artifactId>
    <version>[VERSION]</version>
</dependency>
```

> Replace `[VERSION]` with the latest from [Maven Central](https://central.sonatype.com/artifact/io.flamingock/flamingock-template-api).

> **Note:** If your template lives inside a larger Flamingock project that already depends on `flamingock-core-api`, this dependency is included transitively. You only need to declare `flamingock-template-api` explicitly when your template is a **standalone module** or a **separate published library**.

---

## 🚀 Quick Start

### 1. Define your template class

```java
@ChangeTemplate(name = "my-template")
public class MyTemplate
        extends AbstractChangeTemplate<TemplateVoid, TemplateString, TemplateString> {

    @ApplyTemplate
    public void apply(MyService service) {
        service.execute(applyPayload.getValue());
    }

    @RollbackTemplate
    public void rollback(MyService service) {
        service.undo(rollbackPayload.getValue());
    }
}
```

### 2. Register via ServiceLoader

Create a file at `src/main/resources/META-INF/services/io.flamingock.api.template.ChangeTemplate` listing your template's fully qualified class name:

```plaintext
com.example.MyTemplate
```

### 3. Use it in YAML

```yaml
id: apply-my-change
template: my-template
targetSystem:
  id: "my-system"
apply: "some apply payload"
rollback: "some rollback payload"
```

For a complete walkthrough — including custom payload types, multi-step templates, shared configuration, validation, and transaction metadata — see [**Create Your Own Template**](https://docs.flamingock.io/templates/create-your-own-template).

---

## ⚙️ Requirements

- **Java 8** or higher
- **Flamingock Core** for runtime execution of the template

---

## 🌿 GraalVM Native Image Support

`AbstractChangeTemplate` implements `ReflectionMetadataProvider` and **automatically registers** all generic type argument classes (configuration, apply payload, rollback payload) plus `TemplateStep` for GraalVM reflection — no manual `reflect-config.json` required.

If your payload types internally reference other custom classes that need reflection, pass them to the superclass constructor:

```java
public class MyTemplate extends AbstractChangeTemplate<MyConfig, MyApply, MyRollback> {
    public MyTemplate() {
        super(InternalHelper.class, AnotherClass.class);
    }
    // ...
}
```

---

## 🛠️ Building from Source

```bash
./gradlew build
```

## 🧪 Running Tests

```bash
./gradlew test
```

---

## 📘 Learn more

- [Create Your Own Template](https://docs.flamingock.io/templates/create-your-own-template)
- [Templates Introduction](https://docs.flamingock.io/templates/templates-introduction)
- [Flamingock Documentation](https://docs.flamingock.io)
- [Core Concepts](https://docs.flamingock.io/get-started/core-concepts)
- [SQL Template](https://github.com/flamingock/flamingock-java-template-sql) — reference implementation
- [Examples Repository](https://github.com/flamingock/flamingock-java-examples)

---

## 🤝 Contributing

Flamingock is built in the open.

If you'd like to report a bug, suggest an improvement, or contribute code,
please see the main [Flamingock repository](https://github.com/flamingock/flamingock-java).

---

## 📢 Get involved

- Star the project to show support
- Report issues via the [issue tracker](https://github.com/flamingock/flamingock-java-template-api/issues)
- Join discussions on [GitHub Discussions](https://github.com/flamingock/flamingock-java/discussions)

---

## 📜 License

This project is open source under the [Apache License 2.0](LICENSE).
