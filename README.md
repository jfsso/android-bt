Vinli Android Bluetooth SDK
===========================

> This SDK and accompanying documentation is a work in progress.
> There are areas that are still to be completed and finalized.
> We welcome your feedback. Please post any errors as issues on GitHub and Vinli engineering will respond as quickly as possible.
> And stay tuned for updates over the next few weeks as we bring more features to the SDK.

An Android client for interacting with the Vinli bluetooth device from within your application.
[Sample App](https://github.com/vinli/android-techcrunch-demo)

Download
--------

You can also depend on this library through Gradle from jcenter:
```groovy
compile 'li.vin:android-bt:1.0.0-beta.11'
```

Conventions
-----------
### [RxJava](https://github.com/ReactiveX/RxJava/wiki)
The developer interfaces with the SDK using reactive Observables and Subscriptions.
All data from the device is streamed via Observable, and the data stream is stopped by unsubscribing from the subscription.

Docs
----

### [JavaDocs](http://vinli.github.io/android-bt/)
