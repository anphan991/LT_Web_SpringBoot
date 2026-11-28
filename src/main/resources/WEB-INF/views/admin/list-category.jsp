<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản trị - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261128-v1">
</head>
<body class="admin-page">
<div class="admin-shell">
    <jsp:include page="/WEB-INF/commons/admin/left.jsp" />

    <div class="admin-workspace">

        <main class="admin-main">
            <div class="admin-stats">
                <div class="admin-stat">
                    <div class="admin-stat-label">Danh mục</div>
                    <div class="admin-stat-value">${totalCategories}</div>
                </div>
                <div class="admin-stat">
                    <div class="admin-stat-label">Tài khoản</div>
                    <div class="admin-stat-value">${totalUsers}</div>
                </div>
                <div class="admin-stat">
                    <div class="admin-stat-label">Quyền hiện tại</div>
                    <div class="admin-stat-value">${sessionScope.account.roleid == 1 ? 'Admin CRUD' : 'Manager'}</div>
                </div>
            </div>

            <c:if test="${param.forbidden == '1'}">
                <div class="admin-alert">Chỉ Quản trị viên (Admin) mới có quyền thêm, sửa, xóa hoặc quản lý chi tiết tài khoản.</div>
            </c:if>
            <c:if test="${not empty dbError}">
                <div class="admin-alert"><c:out value="${dbError}" /></div>
            </c:if>
            <c:if test="${not empty userError}">
                <div class="admin-alert"><c:out value="${userError}" /></div>
            </c:if>
            <c:if test="${param.userAdded == '1'}">
                <div class="admin-alert admin-alert-success">Đã thêm tài khoản mới thành công.</div>
            </c:if>
            <c:if test="${param.userUpdated == '1'}">
                <div class="admin-alert admin-alert-success">Đã cập nhật thông tin tài khoản thành công.</div>
            </c:if>
            <c:if test="${param.userDeleted == '1'}">
                <div class="admin-alert admin-alert-success">Đã xóa tài khoản.</div>
            </c:if>
            <c:if test="${param.userError == 'self'}">
                <div class="admin-alert">Không thể xóa tài khoản đang đăng nhập.</div>
            </c:if>
            <c:if test="${param.userError == 'missing'}">
                <div class="admin-alert">Không tìm thấy tài khoản cần thao tác.</div>
            </c:if>
            <c:if test="${param.userError == 'delete'}">
                <div class="admin-alert">Không thể xóa tài khoản. Vui lòng thử lại.</div>
            </c:if>
            <c:if test="${param.categoryAdded == '1'}">
                <div class="admin-alert admin-alert-success">Đã thêm danh mục mới thành công.</div>
            </c:if>
            <c:if test="${param.categoryUpdated == '1'}">
                <div class="admin-alert admin-alert-success">Đã cập nhật danh mục thành công.</div>
            </c:if>
            <c:if test="${param.categoryDeleted == '1'}">
                <div class="admin-alert admin-alert-success">Đã xóa danh mục.</div>
            </c:if>
            <c:if test="${param.categoryError == 'missing'}">
                <div class="admin-alert">Không tìm thấy danh mục cần thao tác.</div>
            </c:if>
            <c:if test="${param.categoryError == 'delete'}">
                <div class="admin-alert">Không thể xóa danh mục. Vui lòng thử lại.</div>
            </c:if>
            <c:if test="${param.categoryError == 'load'}">
                <div class="admin-alert">Không thể tải thông tin danh mục. Vui lòng thử lại.</div>
            </c:if>

            <section class="admin-panel">
                <div class="admin-panel-header">
                    <div class="admin-panel-title">
                        <h2>Danh mục sản phẩm</h2>
                        <p>Thêm, chỉnh sửa, tìm kiếm, phân trang hoặc xóa danh mục.</p>
                    </div>
                    <div class="admin-toolbar">
                        <form class="admin-search" action="${pageContext.request.contextPath}/admin/category/list" method="get">
                            <c:if test="${not empty userKeyword}">
                                <input type="hidden" name="userKeyword" value="<c:out value='${userKeyword}' />">
                            </c:if>
                            <c:if test="${userRole != null}">
                                <input type="hidden" name="userRole" value="<c:out value='${userRole}' />">
                            </c:if>
                            <c:if test="${userCurrentPage > 1}">
                                <input type="hidden" name="userPage" value="${userCurrentPage}">
                            </c:if>
                            <input type="search" name="keyword" value="<c:out value='${keyword}' />" placeholder="Tìm tên danh mục" aria-label="Tìm danh mục">
                            <button type="submit">Tìm kiếm</button>
                            <c:if test="${not empty keyword}">
                                <c:url value="/admin/category/list" var="resetCateUrl">
                                    <c:if test="${not empty userKeyword}"><c:param name="userKeyword" value="${userKeyword}" /></c:if>
                                    <c:if test="${userRole != null}"><c:param name="userRole" value="${userRole}" /></c:if>
                                    <c:if test="${userCurrentPage > 1}"><c:param name="userPage" value="${userCurrentPage}" /></c:if>
                                </c:url>
                                <a class="admin-button admin-button-secondary" href="${resetCateUrl}">Đặt lại</a>
                            </c:if>
                        </form>
                        <a class="admin-button admin-button-primary" href="<c:url value='/admin/category/add'/>">Thêm danh mục</a>
                    </div>
                </div>

                <div class="admin-table-wrap">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>STT</th>
                                <th>Hình ảnh</th>
                                <th>Tên danh mục</th>
                                <th>ID</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${cateList}" var="cate" varStatus="status">
                                <tr>
                                    <td class="admin-id">${pageOffset + status.index + 1}</td>
                                    <c:url value="/image" var="imgUrl">
                                        <c:param name="fname" value="${cate.icon}" />
                                    </c:url>
                                    <td><div class="admin-thumb"><img src="${cate.externalIcon ? cate.icon : imgUrl}" alt="<c:out value='${cate.catename}' />"></div></td>
                                    <td><strong><c:out value="${cate.catename}" /></strong></td>
                                    <td class="admin-id">#<c:out value="${cate.id}" /></td>
                                    <td>
                                        <div class="admin-actions">
                                            <c:url value="/admin/category/edit" var="editUrl">
                                                <c:param name="id" value="${cate.id}" />
                                            </c:url>
                                            <c:url value="/admin/category/delete" var="deleteUrl">
                                                <c:param name="id" value="${cate.id}" />
                                            </c:url>
                                            <a class="admin-action admin-action-edit" href="${editUrl}">Sửa</a>
                                            <a class="admin-action admin-action-delete" href="${deleteUrl}">Xóa</a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty cateList}">
                                <tr><td class="admin-empty" colspan="5">Không tìm thấy danh mục phù hợp.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <c:if test="${filteredCategories > 0}">
                    <nav class="admin-pagination" aria-label="Phân trang danh mục">
                        <span class="admin-pagination-info">Trang ${currentPage} / ${totalPages} (${filteredCategories} danh mục)</span>
                        <c:if test="${totalPages > 1}">
                            <div class="admin-pagination-links">
                                <c:if test="${currentPage > 1}">
                                    <c:url value="/admin/category/list" var="prevUrl">
                                        <c:param name="page" value="${currentPage - 1}" />
                                        <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                        <c:if test="${not empty userKeyword}"><c:param name="userKeyword" value="${userKeyword}" /></c:if>
                                        <c:if test="${userRole != null}"><c:param name="userRole" value="${userRole}" /></c:if>
                                        <c:if test="${userCurrentPage > 1}"><c:param name="userPage" value="${userCurrentPage}" /></c:if>
                                    </c:url>
                                    <a class="admin-page-link" href="${prevUrl}">Trước</a>
                                </c:if>
                                <c:forEach begin="1" end="${totalPages}" var="p">
                                    <c:url value="/admin/category/list" var="pageUrl">
                                        <c:param name="page" value="${p}" />
                                        <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                        <c:if test="${not empty userKeyword}"><c:param name="userKeyword" value="${userKeyword}" /></c:if>
                                        <c:if test="${userRole != null}"><c:param name="userRole" value="${userRole}" /></c:if>
                                        <c:if test="${userCurrentPage > 1}"><c:param name="userPage" value="${userCurrentPage}" /></c:if>
                                    </c:url>
                                    <a class="admin-page-link ${p == currentPage ? 'active' : ''}" href="${pageUrl}">${p}</a>
                                </c:forEach>
                                <c:if test="${currentPage < totalPages}">
                                    <c:url value="/admin/category/list" var="nextUrl">
                                        <c:param name="page" value="${currentPage + 1}" />
                                        <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                        <c:if test="${not empty userKeyword}"><c:param name="userKeyword" value="${userKeyword}" /></c:if>
                                        <c:if test="${userRole != null}"><c:param name="userRole" value="${userRole}" /></c:if>
                                        <c:if test="${userCurrentPage > 1}"><c:param name="userPage" value="${userCurrentPage}" /></c:if>
                                    </c:url>
                                    <a class="admin-page-link" href="${nextUrl}">Sau</a>
                                </c:if>
                            </div>
                        </c:if>
                    </nav>
                </c:if>
            </section>

            <section class="admin-panel" id="users-section">
                <div class="admin-panel-header">
                    <div class="admin-panel-title">
                        <h2>Tài khoản</h2>
                        <p>Quản lý tài khoản, tìm kiếm, phân trang và phân quyền truy cập.</p>
                    </div>
                    <div class="admin-toolbar">
                        <form class="admin-search" action="${pageContext.request.contextPath}/admin/category/list#users-section" method="get">
                            <c:if test="${not empty keyword}">
                                <input type="hidden" name="keyword" value="<c:out value='${keyword}' />">
                            </c:if>
                            <c:if test="${currentPage > 1}">
                                <input type="hidden" name="page" value="${currentPage}">
                            </c:if>
                            <input type="search" name="userKeyword" value="<c:out value='${userKeyword}' />" placeholder="Tìm username, tên, email, SĐT" aria-label="Tìm tài khoản">
                            <select name="userRole" aria-label="Lọc theo vai trò">
                                <option value="" ${userRole == null ? 'selected' : ''}>Tất cả vai trò</option>
                                <option value="1" ${userRole == 1 ? 'selected' : ''}>Admin</option>
                                <option value="2" ${userRole == 2 ? 'selected' : ''}>Manager</option>
                                <option value="5" ${userRole == 5 ? 'selected' : ''}>Member</option>
                            </select>
                            <button type="submit">Tìm kiếm</button>
                            <c:if test="${not empty userKeyword || userRole != null}">
                                <c:url value="/admin/category/list" var="resetUserUrl">
                                    <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                    <c:if test="${currentPage > 1}"><c:param name="page" value="${currentPage}" /></c:if>
                                </c:url>
                                <a class="admin-button admin-button-secondary" href="${resetUserUrl}#users-section">Đặt lại</a>
                            </c:if>
                        </form>
                        <c:if test="${sessionScope.account.roleid == 1}">
                            <a class="admin-button admin-button-secondary" href="${pageContext.request.contextPath}/admin/user/list">Trang quản lý riêng</a>
                            <a class="admin-button admin-button-primary" href="${pageContext.request.contextPath}/admin/user/add">Thêm tài khoản</a>
                        </c:if>
                    </div>
                </div>

                <div class="admin-table-wrap">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>STT</th>
                                <th>ID</th>
                                <th>Tên đăng nhập</th>
                                <th>Họ và tên</th>
                                <th>Liên hệ</th>
                                <th>Vai trò &amp; Trạng thái</th>
                                <c:if test="${sessionScope.account.roleid == 1}"><th>Thao tác</th></c:if>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${userList}" var="u" varStatus="uStatus">
                                <tr>
                                    <td class="admin-id">${userPageOffset + uStatus.index + 1}</td>
                                    <td class="admin-id">#<c:out value="${u.id}" /></td>
                                    <td><strong><c:out value="${u.userName}" /></strong></td>
                                    <td><c:out value="${u.fullName}" /></td>
                                    <td class="admin-id"><c:out value="${not empty u.email ? u.email : '—'}" /><c:if test="${not empty u.phone}"> - <c:out value="${u.phone}" /></c:if></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.roleid == 1}"><span class="admin-role">Admin</span></c:when>
                                            <c:when test="${u.roleid == 2}"><span class="admin-role">Manager</span></c:when>
                                            <c:otherwise><span class="admin-role member">Member</span></c:otherwise>
                                        </c:choose>
                                        <c:choose>
                                            <c:when test="${u.status == 0}">
                                                <span class="admin-role" style="background:#fff2e8;color:#d4380d;border:1px solid #ffbb96;margin-left:4px;">Chờ kích hoạt</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="admin-role" style="background:#f6ffed;color:#389e0d;border:1px solid #b7eb8f;margin-left:4px;">Đã kích hoạt</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <c:if test="${sessionScope.account.roleid == 1}">
                                        <td>
                                            <div class="admin-actions">
                                                <c:url value="/admin/user/edit" var="userEditUrl">
                                                    <c:param name="id" value="${u.id}" />
                                                </c:url>
                                                <a class="admin-action admin-action-edit" href="${userEditUrl}">Sửa</a>
                                                <c:if test="${u.id != sessionScope.account.id}">
                                                    <c:url value="/admin/user/delete" var="userDeleteUrl">
                                                        <c:param name="id" value="${u.id}" />
                                                    </c:url>
                                                    <a class="admin-action admin-action-delete" href="${userDeleteUrl}">Xóa</a>
                                                </c:if>
                                            </div>
                                        </td>
                                    </c:if>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty userList}">
                                <tr><td class="admin-empty" colspan="${sessionScope.account.roleid == 1 ? 7 : 6}">Không tìm thấy tài khoản phù hợp.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <c:if test="${filteredUsers > 0}">
                    <nav class="admin-pagination" aria-label="Phân trang tài khoản">
                        <span class="admin-pagination-info">Trang ${userCurrentPage} / ${userTotalPages} (${filteredUsers} tài khoản)</span>
                        <c:if test="${userTotalPages > 1}">
                            <div class="admin-pagination-links">
                                <c:if test="${userCurrentPage > 1}">
                                    <c:url value="/admin/category/list" var="userPrevUrl">
                                        <c:param name="userPage" value="${userCurrentPage - 1}" />
                                        <c:if test="${not empty userKeyword}"><c:param name="userKeyword" value="${userKeyword}" /></c:if>
                                        <c:if test="${userRole != null}"><c:param name="userRole" value="${userRole}" /></c:if>
                                        <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                        <c:if test="${currentPage > 1}"><c:param name="page" value="${currentPage}" /></c:if>
                                    </c:url>
                                    <a class="admin-page-link" href="${userPrevUrl}#users-section">Trước</a>
                                </c:if>
                                <c:forEach begin="1" end="${userTotalPages}" var="up">
                                    <c:url value="/admin/category/list" var="userPageUrl">
                                        <c:param name="userPage" value="${up}" />
                                        <c:if test="${not empty userKeyword}"><c:param name="userKeyword" value="${userKeyword}" /></c:if>
                                        <c:if test="${userRole != null}"><c:param name="userRole" value="${userRole}" /></c:if>
                                        <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                        <c:if test="${currentPage > 1}"><c:param name="page" value="${currentPage}" /></c:if>
                                    </c:url>
                                    <a class="admin-page-link ${up == userCurrentPage ? 'active' : ''}" href="${userPageUrl}#users-section">${up}</a>
                                </c:forEach>
                                <c:if test="${userCurrentPage < userTotalPages}">
                                    <c:url value="/admin/category/list" var="userNextUrl">
                                        <c:param name="userPage" value="${userCurrentPage + 1}" />
                                        <c:if test="${not empty userKeyword}"><c:param name="userKeyword" value="${userKeyword}" /></c:if>
                                        <c:if test="${userRole != null}"><c:param name="userRole" value="${userRole}" /></c:if>
                                        <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                        <c:if test="${currentPage > 1}"><c:param name="page" value="${currentPage}" /></c:if>
                                    </c:url>
                                    <a class="admin-page-link" href="${userNextUrl}#users-section">Sau</a>
                                </c:if>
                            </div>
                        </c:if>
                    </nav>
                </c:if>
            </section>
        </main>
    </div>
</div>
</body>
</html>
