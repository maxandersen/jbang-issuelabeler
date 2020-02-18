FROM quay.io/maxandersen/jbang-action

ADD depfetch.java /depfetch.java
RUN /jbang/bin/jbang /depfetch.java

ADD issue_labeler.java /issue_labeler.java

RUN GITHUB_TOKEN=blah GITHUB_EVENTPATH=fake /jbang/bin/jbang issue_labeler.java init

ENTRYPOINT ["/jbang/bin/jbang", "/issue_labeler.java"]
