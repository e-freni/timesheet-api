# timesheet-api

Link to ui-project: https://github.com/e-freni/timesheet-ui

This project was born out of a professional need of mine. At my current workplace, each employee has to fill out a
timesheet at the end of the month. This, in itself, creates minor but repetitive problems over time, as well as time
lost in adjusting the template as the days change and a fairly custom organization of the timesheet.

This wouldn't be a problem if it weren't for the fact that the payroll office receives many sheets, each done in their
own way (some people record leaves without marking them or by putting in deficient hours, some note overtime in the
notes section without following the template, etc.), inevitably causing discrepancies and errors.

For my peace of mind, I decided to develop an application that makes it very easy to record workdays using an intuitive
UI that logs all days and allows not only export in the template required by the payroll consultant but also automatic
email sending.

### Objective

Initially, the application was intended for the company I work for, but despite the enthusiasm during the first demo,
the company decided not to adopt the proposed solution. However, I continue to use it with great satisfaction (and the
payroll office continues to make errors, but at least not on my timesheets).

### Architecture

- **Backend (BE)**: Developed in Java using the [Spring Boot](https://spring.io/projects/spring-boot) framework.
- **Frontend (FE)**: Developed in JavaScript/[TypeScript](https://www.typescriptlang.org/) using
  the [Angular](https://angular.dev/) framework.
- **Database**: I used Postres 14, but any relational database can be used, just edit di application.properties inside
  the project and add the driver inside pom.xml

### Features

1. **Workday Logging**: One-click day registration.
2. **Automatic Hour Calculation**: Automatic distribution of hours according to the segments provided by the payroll
   consultant.
3. **Secure Login**: Encrypted passwords and a reset mechanism.
4. **API Protection**: Role-based user permissions (implemented
   with [Spring Security](https://spring.io/projects/spring-security)).
5. **Holiday/Error Management**: Indication of holidays and reminders for missing hours in the last workweek.
6. **Statistics**: Visualization of logged hours, missing hours, and used leave and permits.
7. **Intuitive UI**: Simple and user-friendly interface.
8. **Dark Mode**: Dark mode inspired by IntelliJ colors, implemented with [Tailwind](https://tailwindcss.com/) (I love
   Tailwind!).
9. **Containerization**: Use of Docker for both BE and FE (one container each).
10. **GitHub Integration**: Integrated build, tag, and release structure (deployment to an external server is missing;
    the application runs locally with Apache).
11. **Technical Playgrounds**: Integration of Maven plugins for auto version bumping and jgitflow for development flow
    management are examples of experiments I conducted to understand how to use libraries effectively.

### Conclusion

I used this application as a learning tool to understand how to structure a complete application, covering all its
logical components and addressing every step, until deployment through pipelines. This project effectively demonstrates
my development skills and my ability to organize an application from start to finish. 
