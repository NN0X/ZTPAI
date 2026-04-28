from django.shortcuts import render, get_object_or_404
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
import json
from .models import Product
from .dto import ProductRequestDTO, ProductResponseDTO
from .middleware import ValidationError
from .jwt_auth import require_auth, require_admin


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
        "wersja aplikacji": "1.0.0"
    }
    return JsonResponse(data)


@csrf_exempt
@require_auth
def product_list_create(request):
    if request.method == 'GET':
        products = Product.objects.all()
        data = [ProductResponseDTO(p).to_dict() for p in products]
        return JsonResponse(data, safe=False)

    elif request.method == 'POST':
        body = json.loads(request.body)
        dto = ProductRequestDTO(body)
        errors = dto.validate()
        if errors:
            raise ValidationError(errors)
        product = Product.objects.create(
            name=dto.name,
            price=dto.price,
            description=dto.description,
        )
        return JsonResponse(ProductResponseDTO(product).to_dict(), status=201)

    return JsonResponse({'error': 'Method not allowed'}, status=405)


@csrf_exempt
@require_auth
def product_detail(request, id):
    product = get_object_or_404(Product, id=id)
    return JsonResponse(ProductResponseDTO(product).to_dict())


@csrf_exempt
@require_admin
def product_delete(request, id):
    product = get_object_or_404(Product, id=id)
    if request.method == 'DELETE':
        product.delete()
        return JsonResponse({'message': 'Record deleted succesfully'}, status=200)
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
