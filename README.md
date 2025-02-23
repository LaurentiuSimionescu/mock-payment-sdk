# Mock Payment SDK

## Overview
This is a demo project showcasing a payment processing SDK with built-in safety mechanisms to prevent duplicate transactions. The project is structured for scalability, testing, and CI/CD readiness.

## Features
- **Prevents duplicate transactions** – Ensures a user cannot send the same request twice.
- **Thread-safe & Singleton SDK** – Cannot be instantiated twice, ensuring safety.
- **Environment-ready (.env)** – Supports CI/CD, test, and production environments.
- **Layered architecture** – Improves code readability and maintainability.
- **Tests** – Covers multiple scenarios for reliability.
- **Builder pattern** – Simplifies SDK instantiation.
- **Real Mock API** - Simulate real calls to a mock server: https://run.mocky.io/v3/2bab31a9-a4a5-4a14-9f67-6a5f48f6fb37/payment
- **Use String for Amount** - To avoid floating point issues, and limitations over the network.

## Project Structure
- **PaymentProcessor** – Handles payments and ensures thread safety.
- **PaymentRepository** – Abstracts network requests.
- **PaymentApi** – Defines the API contract.
- **PaymentSDK** – SDK itself, destined for integration.

## Todo
- **Implement micro-factional amount** - To avoid floating point issues.
    Reference: https://blog.agentrisk.com/you-better-work-in-cents-not-dollars-2edb52cdf308
    Reference: https://cardinalby.github.io/blog/post/best-practices/storing-currency-values-data-types/?utm_source=chatgpt.com