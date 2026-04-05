from django.shortcuts import render, get_object_or_404
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
import json
from .models import Product
from .dto import ProductRequestDTO, ProductResponseDTO
from .middleware import ValidationError

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

@csrf_exempt
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

    return JsonResponse({'error': 'Metoda niedozwolona'}, status=405)

@csrf_exempt
def product_detail_delete(request, id):
    product = get_object_or_404(Product, id=id)

    if request.method == 'GET':
        return JsonResponse(ProductResponseDTO(product).to_dict())
    elif request.method == 'DELETE':
        product.delete()
        return JsonResponse({'message': 'Rekord usunięty pomyślnie'}, status=204)

    return JsonResponse({'error': 'Metoda niedozwolona'}, status=405)
