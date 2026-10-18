<%@ page pageEncoding="UTF-8" %>
<%@ include file="_top.jspf" %>
<section class="panel error-panel"><p class="eyebrow">LET'S CHECK THAT</p><h1>We couldn't complete that request.</h1><p role="alert"><c:out value="${message}" default="Please open the planner and try again."/></p><c:if test="${not empty submittedValues}"><h2>Your entered values</h2><p>Keep these values before returning to the planner. No successful save has been confirmed.</p><dl><c:forEach items="${submittedValues}" var="field"><dt><c:out value="${field.key}"/></dt><dd><c:out value="${field.value}"/></dd></c:forEach></dl></c:if><a class="button" href="<c:url value='/'/>">Return to planner</a> <a class="text-link" href="<c:url value='/help'/>">Open help</a></section>
<%@ include file="_bottom.jspf" %>
