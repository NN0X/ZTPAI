import json
import jwt
import datetime
from django.conf import settings
from django.contrib.auth.models import User
from django.contrib.auth import authenticate
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt


@csrf_exempt
def register(request):
    if request.method != 'POST':
        return JsonResponse({'error': 'Method not allowed'}, status=405)

    try:
        data = json.loads(request.body)
    except (json.JSONDecodeError, ValueError):
        return JsonResponse({'error': 'Incorrect JSON'}, status=400)

    username = data.get('username', '').strip()
    password = data.get('password', '')
    role = data.get('role', 'USER').upper()

    if not username or not password:
        return JsonResponse({'error': 'Fields username and password are required'}, status=400)

    if role not in ('USER', 'ADMIN'):
        return JsonResponse({'error': 'Role has to be USER or ADMIN'}, status=400)

    if User.objects.filter(username=username).exists():
        return JsonResponse({'error': 'User with this name already exists'}, status=409)

    is_staff = (role == 'ADMIN')
    user = User.objects.create_user(username=username, password=password, is_staff=is_staff)

    return JsonResponse({
        'message': 'User registered successfully',
        'username': user.username,
        'role': 'ADMIN' if user.is_staff else 'USER',
    }, status=201)


@csrf_exempt
def login(request):
    if request.method != 'POST':
        return JsonResponse({'error': 'Method not allowed'}, status=405)

    try:
        data = json.loads(request.body)
    except (json.JSONDecodeError, ValueError):
        return JsonResponse({'error': 'Incorrect JSON'}, status=400)

    username = data.get('username', '')
    password = data.get('password', '')

    user = authenticate(username=username, password=password)
    if not user:
        return JsonResponse({'error': 'Wrong login credentials'}, status=401)

    role = 'ADMIN' if user.is_staff else 'USER'
    payload = {
        'user_id': user.id,
        'username': user.username,
        'role': role,
        'exp': datetime.datetime.utcnow() + datetime.timedelta(hours=24),
        'iat': datetime.datetime.utcnow(),
    }
    token = jwt.encode(payload, settings.SECRET_KEY, algorithm='HS256')

    return JsonResponse({
        'token': token,
        'username': user.username,
        'role': role,
    })
