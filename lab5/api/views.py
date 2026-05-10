"""
Warstwa widoków (kontrolerów).

Widoki są celowo "cienkie" - parsują request, wołają warstwę
serwisową i zwracają JsonResponse. Cała logika biznesowa siedzi
w api/services.py i jest pokryta testami jednostkowymi (api/tests.py).
"""

import json

from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt

from .jwt_auth import require_auth, require_admin
from .services import ProductService


# Pojedyncza instancja serwisu wystarczy - jest stateless.
_product_service = ProductService()


# ---------------------------------------------------------------------
# Endpointy demo (zostawione z poprzednich labów)
# ---------------------------------------------------------------------
def hello_world(request):
    return HttpResponse("Hello, World!")


def hello_name(request, name):
    return HttpResponse(f"Hello, {name}!")


def greet(request):
    name = request.GET.get('name', 'Guest')
    return HttpResponse(f"Hello, {name}!")


def info(request):
    data = {
        "autor": "Tomasz Sarkowicz",
        "framework": "Django",
        "wersja aplikacji": "1.0.0",
        "lab": 5,
    }
    return JsonResponse(data)


# ---------------------------------------------------------------------
# Endpointy produktów - cienkie warstwy nad ProductService
# ---------------------------------------------------------------------
@csrf_exempt
@require_auth
def product_list_create(request):
    if request.method == 'GET':
        data = _product_service.list_products()
        return JsonResponse(data, safe=False)

    elif request.method == 'POST':
        body = json.loads(request.body)
        result = _product_service.create_product(body)
        return JsonResponse(result, status=201)

    return JsonResponse({'error': 'Method not allowed'}, status=405)


@csrf_exempt
@require_auth
def product_detail(request, id):
    return JsonResponse(_product_service.get_product(id))


@csrf_exempt
@require_admin
def product_delete(request, id):
    if request.method == 'DELETE':
        result = _product_service.delete_product(id)
        return JsonResponse(result, status=200)
    return JsonResponse({'error': 'Method not allowed'}, status=405)


@csrf_exempt
@require_admin
def admin_user_list(request):
    from django.contrib.auth.models import User
    if request.method == 'GET':
        users = User.objects.all().values('id', 'username', 'is_staff', 'date_joined')
        data = [
            {
                'id': u['id'],
                'username': u['username'],
                'role': 'ADMIN' if u['is_staff'] else 'USER',
                'date_joined': u['date_joined'].isoformat(),
            }
            for u in users
        ]
        return JsonResponse(data, safe=False)
    return JsonResponse({'error': 'Method not allowed'}, status=405)
