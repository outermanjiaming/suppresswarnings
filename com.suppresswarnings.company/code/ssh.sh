ssh -CqTfnN -R 0.0.0.0:1234:localhost:22 ubuntu@test.server
ssh -fCNL "*:1235:localhost:1234" ubuntu@test.server
ssh -p 1235 ubuntu@test.server