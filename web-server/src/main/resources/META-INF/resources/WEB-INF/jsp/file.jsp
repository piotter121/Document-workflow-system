<%--@elvariable id="_csrf" type="org.springframework.security.web.csrf.DefaultCsrfToken"--%>
<%--@elvariable id="currentUser" type="pl.edu.pw.ee.pyskp.documentworkflow.dtos.UserInfoDTO"--%>
<%--@elvariable id="task" type="pl.edu.pw.ee.pyskp.documentworkflow.dtos.TaskSummaryDTO"--%>
<%--@elvariable id="file" type="pl.edu.pw.ee.pyskp.documentworkflow.dtos.FileMetadataDTO"--%>
<%--@elvariable id="projectId" type="java.lang.String"--%>
<%--@elvariable id="taskAdministrator" type="pl.edu.pw.ee.pyskp.documentworkflow.dtos.UserInfoDTO"--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="css.jsp" %>
    <title>System obiegu dokumentów - ${file.name}</title>
</head>

<body>

<div class="page-header">
    <h1>
        <img src="<spring:url value="/images/logo.png"/>" width="40px" height="40px">
        <a href="<spring:url value="/projects/${projectId}/tasks/${task.id}"/>">
            ${task.name}
        </a>
        <small>${file.name}</small>
    </h1>
</div>

<%@ include file="navbarTaskActive.jsp" %>

<div class="container-fluid">
    <div class="row">
        <div class="col-md-8">
            <div class="toolbar" role="toolbar">
                <c:if test="${not file.confirmed}">
                    <div class="btn-group">
                        <c:if test="${currentUser != taskAdministrator and not file.markedToConfirm}">
                            <button form="markToConfirm" type="submit"
                                    class="btn btn-success">
                                <span class="glyphicon glyphicon-ok-sign"></span>
                                Zaznacz do zatwierdzenia
                            </button>
                        </c:if>
                        <a href="<spring:url value="/projects/${projectId}/tasks/${task.id}/files/${file.id}/versions/add"/>"
                           class="btn btn-primary">
                            <span class="glyphicon glyphicon-plus"></span>
                            Dodaj nową wersję
                        </a>
                    </div>
                    <c:if test="${currentUser != taskAdministrator}">
                        <form id="markToConfirm" method="post"
                              action="<spring:url value="/projects/${projectId}/tasks/${task.id}/files/${file.id}/markToConfirm"/>">
                            <input type="hidden" name="${_csrf.parameterName}"
                                   value="${_csrf.token}"/>
                            <input type="hidden" name="_method" value="put"/>
                        </form>
                    </c:if>
                </c:if>
                <c:if test="${currentUser eq taskAdministrator}">
                    <div class="btn-group">
                        <c:if test="${not file.confirmed}">
                            <button form="confirm" type="submit" class="btn btn-success">
                                <span class="glyphicon glyphicon-ok"></span>
                                Zatwierdź plik
                            </button>
                        </c:if>
                        <button form="delete" type="submit" class="btn btn-danger">
                            <span class="glyphicon glyphicon-remove"></span>
                            Usuń plik
                        </button>
                    </div>
                    <c:if test="${not file.confirmed}">
                        <form id="confirm" method="post"
                              action="<spring:url value="/projects/${projectId}/tasks/${task.id}/files/${file.id}/confirm"/>">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        </form>
                    </c:if>
                    <form id="delete" method="post"
                          action="<spring:url value="/projects/${projectId}/tasks/${task.id}/files/${file.id}"/>">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <input type="hidden" name="_method" value="delete"/>
                    </form>
                </c:if>
            </div>

            <div class="text-center">
                <h3>Zapisane wersje pliku ${file.name}</h3>
            </div>
            <div id="versions" class="list-group">
                <c:forEach items="${file.versionSortedBySaveDateDESC}" var="version">
                    <div class="list-group-item">
                        <span class="badge">Wersja ${version.versionString}</span>
                        <c:if test="${version.saveDate eq file.modificationDate}">
                            <span class="badge">Najnowsza wersja</span>
                        </c:if>
                        <div class="list-group-item-text">
                            <div class="row">
                                <div class="col-md-5">
                                    <dl class="dl-horizontal">
                                        <dt>Ilość zmian:</dt>
                                        <dd>${version.numberOfDifferences}</dd>
                                        <dt>Ilość zmienionych linii:</dt>
                                        <dd>${version.numberOfModifiedLines}</dd>
                                        <dt>Ilość dodanych linii:</dt>
                                        <dd>${version.numberOfInsertedLines}</dd>
                                        <dt>Ilość usuniętych linii:</dt>
                                        <dd>${version.numberOfDeletedLines}</dd>
                                    </dl>
                                </div>
                                <blockquote class="col-md-7">
                                    <p>${version.message}</p>
                                    <footer>
                                            ${version.author.fullName} w dniu <fmt:formatDate
                                            value="${version.saveDate}"
                                            type="both" dateStyle="long"
                                            timeStyle="short"/>
                                    </footer>
                                </blockquote>
                                <div class="toolbar col-md-12" role="toolbar">
                                    <div class="btn-group">
                                        <a class="btn btn-primary" target="_blank"
                                           href="<spring:url value="/projects/${projectId}/tasks/${task.id}/files/${file.id}/versions/${version.saveDate.time}/content"/>">
                                            <span class="glyphicon glyphicon-download"></span>
                                            Pobierz plik
                                        </a>
                                        <a class="btn btn-info"
                                           href="<spring:url value="/projects/${projectId}/tasks/${task.id}/files/${file.id}/versions/${version.saveDate.time}"/>">
                                            <span class="glyphicon glyphicon-info-sign"></span>
                                            Szczegóły
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>

        <div class="col-md-4">
            <div class="panel panel-info">
                <div class="panel-heading">
                    Szczegóły pliku
                </div>
                <div class="list-group">
                    <div class="list-group-item">
                        <h6 class="list-group-item-heading">Nazwa</h6>
                        <p class="list-group-item-text">${file.name}</p>
                    </div>
                    <div class="list-group-item">
                        <h6 class="list-group-item-heading">Opis</h6>
                        <p class="list-group-item-text">${file.description}</p>
                    </div>
                    <div class="list-group-item">
                        <h6 class="list-group-item-heading">Typ zawartości</h6>
                        <p class="list-group-item-text">${file.contentType}</p>
                    </div>
                    <div class="list-group-item">
                        <h6 class="list-group-item-heading">Data utworzenia</h6>
                        <p class="list-group-item-text"><fmt:formatDate value="${file.creationDate}" type="both"
                                                                        dateStyle="long" timeStyle="long"/></p>
                    </div>
                    <div class="list-group-item">
                        <h6 class="list-group-item-heading">Data modyfikacji</h6>
                        <p class="list-group-item-text"><fmt:formatDate value="${file.modificationDate}" type="both"
                                                                        dateStyle="long" timeStyle="long"/></p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>