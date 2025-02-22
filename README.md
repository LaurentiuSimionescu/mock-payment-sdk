

demo project

makes payment, has safety for the user not to send the same request twice
.env is used tq be ready for CI CD, test and prod environments
tests are written for the project
there are a few layers of abstraction to make the code more readable and maintainable
the SDK cannot be instantiated twice and it is thread safe same for the payment

todo:
dont expose all classed externally