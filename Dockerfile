FROM quay.io/maxandersen/jbang-action

ADD issue_labeler.java /issue_labeler.java

RUN /jbang/bin/jbang issue_labeler.java init

ENTRYPOINT ["/jbang/bin/jbang", "/issue_labeler.java"]
