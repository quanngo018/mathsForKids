# MNIST Model Instructions

To complete the handwriting recognition feature, you need to add a pretrained MNIST TensorFlow Lite model.

## Option 1: Download a Pre-converted Model
Download `mnist.tflite` from one of these sources:
- TensorFlow Hub: https://tfhub.dev/tensorflow/lite-model/mnist/1/default/1
- GitHub repositories with MNIST TFLite models

## Option 2: Convert Your Own Model

If you have a TensorFlow/Keras MNIST model, convert it using:

```python
import tensorflow as tf

# Load your trained model
model = tf.keras.models.load_model('mnist_model.h5')

# Convert to TFLite
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save the model
with open('mnist.tflite', 'wb') as f:
    f.write(tflite_model)
```

## Option 3: Use a Simple Pretrained Model

Here's a simple script to train and convert an MNIST model:

```python
import tensorflow as tf
from tensorflow import keras

# Load MNIST dataset
(x_train, y_train), (x_test, y_test) = keras.datasets.mnist.load_data()

# Normalize pixel values
x_train, x_test = x_train / 255.0, x_test / 255.0

# Add channel dimension
x_train = x_train[..., tf.newaxis]
x_test = x_test[..., tf.newaxis]

# Create model
model = keras.Sequential([
    keras.layers.Conv2D(32, 3, activation='relu', input_shape=(28, 28, 1)),
    keras.layers.MaxPooling2D(),
    keras.layers.Flatten(),
    keras.layers.Dense(128, activation='relu'),
    keras.layers.Dropout(0.2),
    keras.layers.Dense(10, activation='softmax')
])

# Compile and train
model.compile(optimizer='adam',
              loss='sparse_categorical_crossentropy',
              metrics=['accuracy'])

model.fit(x_train, y_train, epochs=5, validation_data=(x_test, y_test))

# Convert to TFLite
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save
with open('mnist.tflite', 'wb') as f:
    f.write(tflite_model)
```

## Expected Model Format

The model should:
- Accept input: `[1, 28, 28, 1]` (batch, height, width, channels)
- Output: `[1, 10]` (batch, probabilities for digits 0-9)
- Input values normalized to 0-1 range

Place the `mnist.tflite` file in this directory: `app/src/main/assets/`
