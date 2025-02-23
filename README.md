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

## Project Structure
- **PaymentProcessor** – Handles payments and ensures thread safety.
- **PaymentRepository** – Abstracts network requests.
- **PaymentApi** – Defines the API contract.
- **PaymentSDK** – SDK itself, destined for integration.