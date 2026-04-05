from django.urls import path
from . import views

urlpatterns = [
    path('hello/', views.hello_world, name='hello_world'),
    path('hello/<str:name>/', views.hello_name, name='hello_name'),
    path('greet/', views.greet, name='greet'),
    path('info/', views.info, name='info'),

    path('api/products/', views.product_list_create, name='product_list_create'),
    path('api/products/<int:id>/', views.product_detail_delete, name='product_detail_delete'),
]
