from django.urls import path
from . import views
from . import auth_views

urlpatterns = [
    path('hello/', views.hello_world, name='hello_world'),
    path('hello/<str:name>/', views.hello_name, name='hello_name'),
    path('greet/', views.greet, name='greet'),
    path('info/', views.info, name='info'),

    path('api/auth/register/', auth_views.register, name='auth_register'),
    path('api/auth/login/',    auth_views.login,    name='auth_login'),

    path('api/products/',       views.product_list_create, name='product_list_create'),
    path('api/products/<int:id>/', views.product_detail,   name='product_detail'),

    path('api/admin/products/<int:id>/', views.product_delete,  name='product_delete'),
    path('api/admin/users/',             views.admin_user_list,  name='admin_user_list'),
]
