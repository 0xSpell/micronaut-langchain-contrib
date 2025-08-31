# Micronaut Langchain Contrib

This package contains micronaut-driven extensions for langchain.
A tool processor picks up method annotated with `@AiTool` and registers them in the langchain context.
Ultimately, this is intended to allow running arbitrary methods as tools in LLMs, while supporting micronaut 
parameter injection as used by HTTP endpoints.

This is still a work in progress, but already somewhat usable.

Contributions are welcome!