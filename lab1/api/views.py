from django.shortcuts import render
from django.http import HttpResponse, JsonResponse

def hello_world(request):
    return HttpResponse("Hello, World!")

def hello_name(request, name):
    return HttpResponse(f"Hello, {name}!")

def greet(request):
    name = request.GET.get('name', 'Gość')
    return HttpResponse(f"Hello, {name}!")

def info(request):
    data = {
        "autor": "Tomasz Sarkowicz",
        "framework": "Django",
        "wersja aplikacji": "1.0.0"
    }
    return JsonResponse(data)
