"""
Simple script to train and convert an MNIST model to TensorFlow Lite
Run this script with: python create_mnist_model.py
"""

import tensorflow as tf
from tensorflow import keras
import numpy as np

print("Loading MNIST dataset...")
(x_train, y_train), (x_test, y_test) = keras.datasets.mnist.load_data()

# Normalize pixel values to 0-1 range
x_train = x_train.astype('float32') / 255.0
x_test = x_test.astype('float32') / 255.0

# Reshape for the model (add channel dimension)
x_train = x_train.reshape(-1, 28, 28, 1)
x_test = x_test.reshape(-1, 28, 28, 1)

print("Creating model...")
model = keras.Sequential([
    keras.layers.Conv2D(32, (3, 3), activation='relu', input_shape=(28, 28, 1)),
    keras.layers.MaxPooling2D((2, 2)),
    keras.layers.Conv2D(64, (3, 3), activation='relu'),
    keras.layers.MaxPooling2D((2, 2)),
    keras.layers.Flatten(),
    keras.layers.Dense(128, activation='relu'),
    keras.layers.Dropout(0.2),
    keras.layers.Dense(10, activation='softmax')
])

print("Compiling model...")
model.compile(
    optimizer='adam',
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

print("Training model...")
model.fit(
    x_train, y_train,
    epochs=30,
    batch_size=128,
    validation_split=0.1,
    verbose=1
)

print("Evaluating model...")
test_loss, test_acc = model.evaluate(x_test, y_test, verbose=0)
print(f"Test accuracy: {test_acc:.4f}")

print("Converting to TensorFlow Lite...")
converter = tf.lite.TFLiteConverter.from_keras_model(model)
# Use compatibility mode for older TFLite runtime
converter.target_spec.supported_ops = [
    tf.lite.OpsSet.TFLITE_BUILTINS,  # Use standard TFLite ops
]
converter.optimizations = [tf.lite.Optimize.DEFAULT]
# Set minimum runtime version for compatibility
converter._experimental_lower_tensor_list_ops = False
tflite_model = converter.convert()

print("Saving model to mnist.tflite...")
with open('mnist.tflite', 'wb') as f:
    f.write(tflite_model)

print("✓ Model saved successfully!")
print(f"Model size: {len(tflite_model) / 1024:.2f} KB")
print("\nNext step: Copy mnist.tflite to app/src/main/assets/")
