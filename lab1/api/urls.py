from django.urls import path
from . import views

urlpatterns = [
    path('hello/', views.hello_world, name='hello_world'),
    path('hello/<str:name>/', views.hello_name, name='hello_name'),
    path('greet/', views.greet, name='greet'),
    path('info/', views.info, name='info'),
]
