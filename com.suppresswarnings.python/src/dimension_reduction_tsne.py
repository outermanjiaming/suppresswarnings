import numpy as np
import matplotlib.pyplot as plt
from sklearn.manifold import TSNE
X = np.array([[0,1,1],[1,0,1],[1,1,0]])
X_embedded = TSNE(n_components=2).fit_transform(X)
Y = X_embedded.transpose()
plt.plot(Y[0], Y[1], "p")
plt.show()