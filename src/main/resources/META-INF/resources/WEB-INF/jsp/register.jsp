<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <%@ include file="css.jsp" %>
    <title>Zarejestruj się</title>
</head>
<body>

<div class="page-header">
    <h1>
        <img src="<spring:url value="/images/logo.png"/>" width="40px" height="40px">
        System obiegu dokumentów
        <small>Rejestracja nowego użytkownika</small>
    </h1>
</div>

<nav class="navbar navbar-inverse">
    <div class="container-fluid">
        <ul class="nav navbar-nav">
            <li><a href="<spring:url value="/login"/>">
                <span class="glyphicon glyphicon-log-in"></span> Zaloguj się</a>
            </li>
        </ul>
    </div>
</nav>

<div class="container-fluid">
    <%--@elvariable id="newUser" type="pl.edu.pw.ee.pyskp.documentworkflow.dtos.NewUserDTO"--%>
    <form:form modelAttribute="newUser" class="form-horizontal">
        <fieldset>
            <legend>
                <spring:message code="register.form.legend"/>
            </legend>
            <div class="form-group">
                <label class="control-label col-md-2" for="login">
                    <spring:message code="register.form.username.label"/>
                </label>
                <div class="col-md-5">
                    <form:input path="login" id="login" type="text" class="form-control"/>
                    <p><form:errors path="login" cssClass="text-danger"/></p>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-md-2" for="email">
                    <spring:message code="register.form.email.label"/>
                </label>
                <div class="col-md-5">
                    <form:input path="email" id="email" type="text" class="form-control"/>
                    <p><form:errors path="email" cssClass="text-danger"/></p>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-md-2" for="firstName">
                    <spring:message code="register.form.firstName.label"/>
                </label>
                <div class="col-md-5">
                    <form:input path="firstName" id="firstName" type="text" class="form-control"/>
                    <p><form:errors path="firstName" cssClass="text-danger"/></p>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-md-2" for="lastName">
                    <spring:message code="register.form.lastName.label"/>
                </label>
                <div class="col-md-5">
                    <form:input path="lastName" id="lastName" type="text" class="form-control"/>
                    <p><form:errors path="lastName" cssClass="text-danger"/></p>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-md-2" for="password">
                    <spring:message code="register.form.password.label"/>
                </label>
                <div class="col-md-5">
                    <form:input path="password" id="password" type="password" class="form-control"/>
                    <p><form:errors path="password" cssClass="text-danger"/></p>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-md-2" for="passwordRepeated">
                    <spring:message code="register.form.passwordRepeated.label"/>
                </label>
                <div class="col-md-5">
                    <form:input path="passwordRepeated" id="passwordRepeated" type="password" class="form-control"/>
                    <p><form:errors path="passwordRepeated" cssClass="text-danger"/></p>
                </div>
            </div>
            <div class="form-group">
                <div class="col-md-offset-2 col-md-10">
                    <button type="submit" id="btnRegister" class="btn btn-primary">
                        <span class="glyphicon glyphicon-check"></span> Zarejestruj się
                    </button>
                </div>
            </div>
        </fieldset>
    </form:form>
</div>

</body>
</html>